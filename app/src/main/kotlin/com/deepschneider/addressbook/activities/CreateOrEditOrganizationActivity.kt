package com.deepschneider.addressbook.activities

import android.os.Bundle
import android.os.Handler
import android.os.Looper
import android.text.Editable
import android.text.TextWatcher
import android.view.MenuItem
import android.view.View
import com.deepschneider.addressbook.R
import com.deepschneider.addressbook.databinding.ActivityCreateNewOrganizationBinding
import com.deepschneider.addressbook.dto.OrganizationDto
import com.deepschneider.addressbook.dto.PageDataDto
import com.deepschneider.addressbook.network.SaveOrCreateEntityRequest
import com.deepschneider.addressbook.utils.Constants
import com.deepschneider.addressbook.utils.Urls
import com.google.android.material.dialog.MaterialAlertDialogBuilder
import com.google.gson.reflect.TypeToken
import java.util.*
import java.util.concurrent.ExecutorService
import java.util.concurrent.Executors

class CreateOrEditOrganizationActivity : AbstractEntityActivity() {

    private lateinit var binding: ActivityCreateNewOrganizationBinding
    private var organizationDto: OrganizationDto? = null
    private val fieldValidation = BooleanArray(4)

    inner class TextFieldValidation(private val view: View) : TextWatcher {
        override fun afterTextChanged(s: Editable?) {}
        override fun beforeTextChanged(s: CharSequence?, start: Int, count: Int, after: Int) {}
        override fun onTextChanged(s: CharSequence?, start: Int, before: Int, count: Int) {
            when (view.id) {
                R.id.zip -> {
                    validateZipEditText()
                }
                R.id.address -> {
                    validateAddressEditText()
                }
                R.id.type -> {
                    validateTypeEditText()
                }
                R.id.name -> {
                    validateNameEditText()
                }
            }
            updateSaveButtonState()
        }
    }

    private fun validateNameEditText() {
        val nameEditText = binding.name
        val nameEditTextLayout = binding.nameLayout
        val value = nameEditText.text.toString().trim()
        if (value.isEmpty()) {
            nameEditTextLayout.error = this.getString(R.string.validation_error_required_field)
            fieldValidation[3] = false
        } else if (value.length > 500) {
            nameEditTextLayout.error = this.getString(R.string.validation_error_value_too_long)
            fieldValidation[3] = false
        } else {
            nameEditTextLayout.error = null
            fieldValidation[3] = true
        }
    }

    private fun validateAddressEditText() {
        val addressEditText = binding.address
        val addressEditTextLayout = binding.addressLayout
        val value = addressEditText.text.toString().trim()
        if (value.isEmpty()) {
            addressEditTextLayout.error = this.getString(R.string.validation_error_required_field)
            fieldValidation[1] = false
        } else if (value.length > 500) {
            addressEditTextLayout.error = this.getString(R.string.validation_error_value_too_long)
            fieldValidation[1] = false
        } else {
            addressEditTextLayout.error = null
            fieldValidation[1] = true
        }
    }

    private fun validateZipEditText() {
        val zipEditText = binding.zip
        val zipEditTextLayout = binding.zipLayout
        val value = zipEditText.text.toString().trim()
        if (value.isEmpty()) {
            zipEditTextLayout.error = this.getString(R.string.validation_error_required_field)
            fieldValidation[0] = false
        } else if (value.length > 100) {
            zipEditTextLayout.error = this.getString(R.string.validation_error_value_too_long)
            fieldValidation[0] = false
        } else {
            zipEditTextLayout.error = null
            fieldValidation[0] = true
        }
    }

    private fun validateTypeEditText() {
        val typeEditText = binding.type
        val typeEditTextLayout = binding.typeLayout
        if (typeEditText.text.toString().trim().isEmpty()) {
            typeEditTextLayout.error = this.getString(R.string.validation_error_required_field)
            fieldValidation[2] = false
        } else {
            typeEditTextLayout.error = null
            fieldValidation[2] = true
        }
    }

    private fun prepareExtras() {
        val extra = intent.extras?.get("organization")
        if (extra != null) {
            organizationDto = extra as OrganizationDto
        }
    }

    private fun prepareLayout() {
        binding.saveCreateButton.setOnClickListener {
            saveOrCreateOrganization()
        }
    }

    private fun prepareTypeEditText() {
        binding.type.setOnClickListener {
            val builder = MaterialAlertDialogBuilder(this@CreateOrEditOrganizationActivity)
            builder.setTitle(R.string.choose_organization_type).setItems(
                R.array.org_types
            ) { dialog, which ->
                if (which == 0) binding.type.text = null
                else binding.type.setText(resources.getStringArray(R.array.org_types)[which])
                dialog.dismiss()
            }
            builder.create().show()
        }
    }

    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        binding = ActivityCreateNewOrganizationBinding.inflate(layoutInflater)
        setContentView(binding.root)
        supportActionBar?.setDisplayHomeAsUpEnabled(true)
        supportActionBar?.setHomeButtonEnabled(true)
        prepareExtras()
        prepareLayout()
        prepareTypeEditText()
        updateUi(organizationDto)
        setupListeners()
        validateTypeEditText()
        validateZipEditText()
        validateAddressEditText()
        validateNameEditText()
        updateSaveButtonState()
    }

    private fun setupListeners() {
        binding.type.addTextChangedListener(TextFieldValidation(binding.type))
        binding.zip.addTextChangedListener(TextFieldValidation(binding.zip))
        binding.address.addTextChangedListener(TextFieldValidation(binding.address))
        binding.name.addTextChangedListener(TextFieldValidation(binding.name))
    }

    private fun updateUi(organizationDto: OrganizationDto?) {
        organizationDto?.let {
            binding.type.setText(it.type)
            binding.zip.setText(it.zip)
            binding.address.setText(it.street)
            binding.name.setText(it.name)
            binding.id.setText(it.id)
            binding.lastUpdated.setText(it.lastUpdated)
            binding.saveCreateButton.text = this.getString(R.string.action_save_changes)
            title = this.getString(R.string.edit_activity_header) + " " + it.name
        } ?: run {
            binding.saveCreateButton.text = this.getString(R.string.action_create)
        }
    }

    private fun convertTypeToIndex(type: String): String {
        return (this.resources.getStringArray(R.array.org_types).indexOf(type) - 1).toString()
    }

    private fun convertIndexToType(type: String): String {
        return this.resources.getStringArray(R.array.org_types)[type.toInt() + 1]
    }

    private fun updateSaveButtonState() {
        binding.saveCreateButton.isEnabled = fieldValidation.all { it }
    }

    private fun saveOrCreateOrganization() {
        var targetOrganizationDto: OrganizationDto? = null
        var create = false
        organizationDto?.let {
            targetOrganizationDto = it
            targetOrganizationDto?.name = binding.name.text.toString()
            targetOrganizationDto?.street = binding.address.text.toString()
            targetOrganizationDto?.zip = binding.zip.text.toString()
            targetOrganizationDto?.type = convertTypeToIndex(binding.type.text.toString())
        } ?: run {
            create = true
            targetOrganizationDto = OrganizationDto()
            targetOrganizationDto?.id = UUID.randomUUID().toString()
            targetOrganizationDto?.name = binding.name.text.toString()
            targetOrganizationDto?.street = binding.address.text.toString()
            targetOrganizationDto?.zip = binding.zip.text.toString()
            targetOrganizationDto?.type = convertTypeToIndex(binding.type.text.toString())
        }
        targetOrganizationDto?.let {
            val handler = Handler(Looper.getMainLooper())
            val executor: ExecutorService = Executors.newSingleThreadExecutor()
            val url = "$serverUrl" + Urls.SAVE_OR_CREATE_ORGANIZATION
            executor.execute {
                requestQueue.add(SaveOrCreateEntityRequest(
                    url,
                    it,
                    { response ->
                        response.data?.let {
                            handler.post {
                                it.type?.let {
                                    response.data?.type = convertIndexToType(it)
                                }
                                organizationDto = it
                                handler.post {
                                    updateUi(it)
                                }
                                organizationDto?.id?.let {
                                    if (create) {
                                        sendLockRequest(
                                            true, Constants.ORGANIZATIONS_CACHE_NAME, it
                                        )
                                        makeSnackBar(
                                            this@CreateOrEditOrganizationActivity.getString(
                                                R.string.organization_created_message
                                            )
                                        )
                                    } else {
                                        makeSnackBar(
                                            this@CreateOrEditOrganizationActivity.getString(
                                                R.string.changes_saved_message
                                            )
                                        )
                                    }
                                }
                            }
                        }
                    },
                    { error ->
                        handler.post {
                            makeErrorSnackBar(error)
                        }
                    },
                    this@CreateOrEditOrganizationActivity,
                    object : TypeToken<PageDataDto<OrganizationDto>>() {}.type
                ).also { it.tag = getRequestTag() })
            }
        }
    }

    override fun getParentCoordinatorLayoutForSnackBar(): View = binding.coordinatorLayout

    override fun getRequestTag(): String = "CREATE_OR_EDIT_ORGANIZATION_TAG"

    override fun onStart() {
        super.onStart()
        organizationDto?.id?.let {
            sendLockRequest(true, Constants.ORGANIZATIONS_CACHE_NAME, it)
        }
    }

    override fun onOptionsItemSelected(item: MenuItem): Boolean {
        return when (item.itemId) {
            android.R.id.home -> {
                finish()
                return true
            }
            else -> super.onOptionsItemSelected(item)
        }
    }

    override fun onStop() {
        super.onStop()
        organizationDto?.id?.let {
            sendLockRequest(false, Constants.ORGANIZATIONS_CACHE_NAME, it)
        }
    }
}