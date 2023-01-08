package com.deepschneider.addressbook.network

import android.content.Context
import com.android.volley.NetworkResponse
import com.android.volley.ParseError
import com.android.volley.Request
import com.android.volley.Response
import com.android.volley.toolbox.HttpHeaderParser
import com.deepschneider.addressbook.dto.FilterDto
import com.deepschneider.addressbook.dto.PageDataDto
import com.deepschneider.addressbook.dto.TableDataDto
import com.deepschneider.addressbook.utils.NetworkUtils
import com.google.gson.Gson
import com.google.gson.JsonSyntaxException
import java.io.UnsupportedEncodingException
import java.lang.reflect.Type

class ListRequest<T>(
    url: String,
    private val filterDto: List<FilterDto>,
    private val responseListener: Response.Listener<PageDataDto<TableDataDto<T>>>,
    errorListener: Response.ErrorListener,
    private var context: Context,
    private val typeToken: Type
) : Request<PageDataDto<TableDataDto<T>>>(Method.POST, url, errorListener) {

    private val gson = Gson()

    override fun getHeaders(): MutableMap<String, String> {
        return NetworkUtils.addAuthHeader(super.getHeaders(), context)
    }

    override fun parseNetworkResponse(response: NetworkResponse?): Response<PageDataDto<TableDataDto<T>>> {
        return try {
            val json = String(response?.data ?: ByteArray(0), Charsets.UTF_8)
            Response.success(
                gson.fromJson(json, typeToken),
                HttpHeaderParser.parseCacheHeaders(response)
            )
        } catch (e: UnsupportedEncodingException) {
            e.printStackTrace()
            return Response.error(ParseError(e))
        } catch (e: JsonSyntaxException) {
            e.printStackTrace()
            return Response.error(ParseError(e))
        }
    }

    override fun getBodyContentType(): String {
        return "application/json; charset=utf-8"
    }

    override fun deliverResponse(response: PageDataDto<TableDataDto<T>>?) {
        responseListener.onResponse(response)
    }

    override fun getBody(): ByteArray {
        return gson.toJson(filterDto).toByteArray(Charsets.UTF_8)
    }
}