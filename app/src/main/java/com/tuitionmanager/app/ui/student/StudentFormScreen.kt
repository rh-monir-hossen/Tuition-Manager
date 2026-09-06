package com.tuitionmanager.app.ui.student

import androidx.compose.foundation.layout.*
import androidx.compose.foundation.rememberScrollState
import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.foundation.text.KeyboardOptions
import androidx.compose.foundation.verticalScroll
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.filled.*
import androidx.compose.material3.*
import androidx.compose.runtime.*
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.res.stringResource
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.text.input.KeyboardType
import androidx.compose.ui.unit.dp
import com.tuitionmanager.app.R
import com.tuitionmanager.app.domain.model.GuardianPreferredChannel
import com.tuitionmanager.app.domain.model.GuardianReportLanguage
import com.tuitionmanager.app.ui.components.FormSectionTitle
import com.tuitionmanager.app.ui.components.LoadingStateView

@OptIn(ExperimentalMaterial3Api::class)
@Composable
fun StudentFormScreen(
    viewModel: StudentFormViewModel,
    onNavigateBack: () -> Unit
) {
    val uiState by viewModel.uiState.collectAsState()

    LaunchedEffect(uiState.isSaveSuccess) {
        if (uiState.isSaveSuccess) {
            onNavigateBack()
        }
    }

    Scaffold(
        topBar = {
            TopAppBar(
                title = {
                    Text(
                        text = if (uiState.isEditMode) stringResource(R.string.title_edit_student)
                        else stringResource(R.string.title_add_student),
                        fontWeight = FontWeight.Bold
                    )
                },
                navigationIcon = {
                    IconButton(onClick = onNavigateBack) {
                        Icon(imageVector = Icons.Default.ArrowBack, contentDescription = stringResource(R.string.action_back))
                    }
                },
                actions = {
                    Button(
                        onClick = { viewModel.saveStudent() },
                        enabled = uiState.isFormValid && !uiState.isSaving,
                        shape = RoundedCornerShape(8.dp),
                        modifier = Modifier.padding(end = 8.dp)
                    ) {
                        if (uiState.isSaving) {
                            CircularProgressIndicator(
                                modifier = Modifier.size(16.dp),
                                strokeWidth = 2.dp,
                                color = MaterialTheme.colorScheme.onPrimary
                            )
                            Spacer(modifier = Modifier.width(6.dp))
                            Text(text = stringResource(R.string.action_saving))
                        } else {
                            Icon(imageVector = Icons.Default.Check, contentDescription = null, modifier = Modifier.size(16.dp))
                            Spacer(modifier = Modifier.width(4.dp))
                            Text(text = stringResource(R.string.action_save))
                        }
                    }
                }
            )
        }
    ) { paddingValues ->
        if (uiState.isLoading) {
            LoadingStateView(message = "Loading student details…")
        } else {
            Column(
                modifier = Modifier
                    .fillMaxSize()
                    .padding(paddingValues)
                    .verticalScroll(rememberScrollState())
                    .padding(horizontal = 16.dp, vertical = 12.dp),
                verticalArrangement = Arrangement.spacedBy(16.dp)
            ) {
                uiState.generalError?.let { error ->
                    Surface(
                        color = MaterialTheme.colorScheme.errorContainer,
                        shape = RoundedCornerShape(8.dp),
                        modifier = Modifier.fillMaxWidth()
                    ) {
                        Text(
                            text = error,
                            color = MaterialTheme.colorScheme.onErrorContainer,
                            style = MaterialTheme.typography.bodyMedium,
                            modifier = Modifier.padding(12.dp)
                        )
                    }
                }

                // Section 1: Basic Information
                FormSectionTitle(title = stringResource(R.string.section_basic_info))

                OutlinedTextField(
                    value = uiState.name,
                    onValueChange = { viewModel.onNameChanged(it) },
                    label = { Text(stringResource(R.string.label_student_name)) },
                    placeholder = { Text(stringResource(R.string.placeholder_student_name)) },
                    isError = uiState.nameError != null,
                    supportingText = uiState.nameError?.let { { Text(it) } },
                    modifier = Modifier.fillMaxWidth(),
                    singleLine = true,
                    shape = RoundedCornerShape(10.dp)
                )

                Row(
                    modifier = Modifier.fillMaxWidth(),
                    horizontalArrangement = Arrangement.spacedBy(12.dp)
                ) {
                    OutlinedTextField(
                        value = uiState.institution,
                        onValueChange = { viewModel.onInstitutionChanged(it) },
                        label = { Text(stringResource(R.string.label_institution)) },
                        placeholder = { Text(stringResource(R.string.placeholder_institution)) },
                        modifier = Modifier.weight(1f),
                        singleLine = true,
                        shape = RoundedCornerShape(10.dp)
                    )

                    OutlinedTextField(
                        value = uiState.classGrade,
                        onValueChange = { viewModel.onClassGradeChanged(it) },
                        label = { Text(stringResource(R.string.label_class_grade)) },
                        placeholder = { Text(stringResource(R.string.placeholder_class_grade)) },
                        modifier = Modifier.weight(1f),
                        singleLine = true,
                        shape = RoundedCornerShape(10.dp)
                    )
                }

                Row(
                    modifier = Modifier.fillMaxWidth(),
                    horizontalArrangement = Arrangement.spacedBy(12.dp)
                ) {
                    OutlinedTextField(
                        value = uiState.phone,
                        onValueChange = { viewModel.onPhoneChanged(it) },
                        label = { Text(stringResource(R.string.label_phone)) },
                        placeholder = { Text(stringResource(R.string.placeholder_phone)) },
                        keyboardOptions = KeyboardOptions(keyboardType = KeyboardType.Phone),
                        modifier = Modifier.weight(1f),
                        singleLine = true,
                        shape = RoundedCornerShape(10.dp)
                    )

                    OutlinedTextField(
                        value = uiState.address,
                        onValueChange = { viewModel.onAddressChanged(it) },
                        label = { Text(stringResource(R.string.label_address)) },
                        placeholder = { Text(stringResource(R.string.placeholder_address)) },
                        modifier = Modifier.weight(1f),
                        singleLine = true,
                        shape = RoundedCornerShape(10.dp)
                    )
                }

                // Section 2: Tuition & Fees
                FormSectionTitle(title = stringResource(R.string.section_tuition_info))

                Row(
                    modifier = Modifier.fillMaxWidth(),
                    horizontalArrangement = Arrangement.spacedBy(12.dp)
                ) {
                    OutlinedTextField(
                        value = uiState.monthlyFee,
                        onValueChange = { viewModel.onMonthlyFeeChanged(it) },
                        label = { Text(stringResource(R.string.label_monthly_fee)) },
                        placeholder = { Text(stringResource(R.string.placeholder_monthly_fee)) },
                        keyboardOptions = KeyboardOptions(keyboardType = KeyboardType.Number),
                        modifier = Modifier.weight(1.2f),
                        singleLine = true,
                        shape = RoundedCornerShape(10.dp)
                    )

                    OutlinedTextField(
                        value = uiState.billingCycleDay,
                        onValueChange = { viewModel.onBillingCycleDayChanged(it) },
                        label = { Text(stringResource(R.string.label_billing_day)) },
                        placeholder = { Text("1–31") },
                        keyboardOptions = KeyboardOptions(keyboardType = KeyboardType.Number),
                        modifier = Modifier.weight(0.8f),
                        singleLine = true,
                        shape = RoundedCornerShape(10.dp)
                    )
                }

                OutlinedTextField(
                    value = uiState.initialSubjects,
                    onValueChange = { viewModel.onInitialSubjectsChanged(it) },
                    label = { Text(stringResource(R.string.label_initial_subjects)) },
                    placeholder = { Text(stringResource(R.string.placeholder_initial_subjects)) },
                    modifier = Modifier.fillMaxWidth(),
                    singleLine = true,
                    shape = RoundedCornerShape(10.dp)
                )

                // Active status switch
                Row(
                    modifier = Modifier
                        .fillMaxWidth()
                        .padding(vertical = 4.dp),
                    horizontalArrangement = Arrangement.SpaceBetween,
                    verticalAlignment = Alignment.CenterVertically
                ) {
                    Column {
                        Text(
                            text = stringResource(R.string.label_is_active),
                            style = MaterialTheme.typography.bodyLarge,
                            fontWeight = FontWeight.Medium
                        )
                        Text(
                            text = "Active students appear in scheduling and quick diary lists",
                            style = MaterialTheme.typography.bodySmall,
                            color = MaterialTheme.colorScheme.onSurfaceVariant
                        )
                    }
                    Switch(
                        checked = uiState.isActive,
                        onCheckedChange = { viewModel.onActiveStatusChanged(it) }
                    )
                }

                // Section 3: Guardian Information & Progress
                FormSectionTitle(title = stringResource(R.string.section_guardian_info))

                Row(
                    modifier = Modifier.fillMaxWidth(),
                    horizontalArrangement = Arrangement.spacedBy(12.dp)
                ) {
                    OutlinedTextField(
                        value = uiState.guardianName,
                        onValueChange = { viewModel.onGuardianNameChanged(it) },
                        label = { Text(stringResource(R.string.label_guardian_name)) },
                        placeholder = { Text(stringResource(R.string.placeholder_guardian_name)) },
                        modifier = Modifier.weight(1f),
                        singleLine = true,
                        shape = RoundedCornerShape(10.dp)
                    )

                    OutlinedTextField(
                        value = uiState.guardianPhone,
                        onValueChange = { viewModel.onGuardianPhoneChanged(it) },
                        label = { Text(stringResource(R.string.label_guardian_phone)) },
                        placeholder = { Text(stringResource(R.string.placeholder_guardian_phone)) },
                        keyboardOptions = KeyboardOptions(keyboardType = KeyboardType.Phone),
                        modifier = Modifier.weight(1f),
                        singleLine = true,
                        shape = RoundedCornerShape(10.dp)
                    )
                }

                // Guardian Communication Channel chips
                Column(verticalArrangement = Arrangement.spacedBy(6.dp)) {
                    Text(
                        text = stringResource(R.string.label_guardian_channel),
                        style = MaterialTheme.typography.labelMedium,
                        color = MaterialTheme.colorScheme.onSurfaceVariant
                    )
                    Row(
                        modifier = Modifier.fillMaxWidth(),
                        horizontalArrangement = Arrangement.spacedBy(8.dp)
                    ) {
                        listOf(
                            GuardianPreferredChannel.WHATSAPP to "WhatsApp",
                            GuardianPreferredChannel.SMS to "SMS",
                            GuardianPreferredChannel.MESSENGER to "Messenger",
                            GuardianPreferredChannel.EMAIL to "Email"
                        ).forEach { (channel, label) ->
                            FilterChip(
                                selected = uiState.guardianChannel == channel,
                                onClick = { viewModel.onGuardianChannelChanged(channel) },
                                label = { Text(label) }
                            )
                        }
                    }
                }

                // Sharing switch
                Row(
                    modifier = Modifier
                        .fillMaxWidth()
                        .padding(vertical = 4.dp),
                    horizontalArrangement = Arrangement.SpaceBetween,
                    verticalAlignment = Alignment.CenterVertically
                ) {
                    Text(
                        text = stringResource(R.string.label_enable_guardian_sharing),
                        style = MaterialTheme.typography.bodyMedium
                    )
                    Switch(
                        checked = uiState.isGuardianProgressSharingEnabled,
                        onCheckedChange = { viewModel.onProgressSharingChanged(it) }
                    )
                }

                // Language options (English vs Bangla)
                Row(
                    modifier = Modifier
                        .fillMaxWidth()
                        .padding(vertical = 4.dp),
                    horizontalArrangement = Arrangement.SpaceBetween,
                    verticalAlignment = Alignment.CenterVertically
                ) {
                    Text(
                        text = stringResource(R.string.label_guardian_language),
                        style = MaterialTheme.typography.bodyMedium
                    )
                    Row(horizontalArrangement = Arrangement.spacedBy(8.dp)) {
                        FilterChip(
                            selected = uiState.guardianReportLanguage == GuardianReportLanguage.EN,
                            onClick = { viewModel.onReportLanguageChanged(GuardianReportLanguage.EN) },
                            label = { Text("English") }
                        )
                        FilterChip(
                            selected = uiState.guardianReportLanguage == GuardianReportLanguage.BN,
                            onClick = { viewModel.onReportLanguageChanged(GuardianReportLanguage.BN) },
                            label = { Text("বাংলা") }
                        )
                    }
                }

                Spacer(modifier = Modifier.height(24.dp))
            }
        }
    }
}
