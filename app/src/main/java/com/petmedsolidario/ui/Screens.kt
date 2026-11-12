package com.petmedsolidario.ui

import androidx.compose.foundation.Image
import androidx.compose.foundation.layout.*
import androidx.compose.foundation.selection.toggleable
import androidx.compose.foundation.text.KeyboardOptions
import androidx.compose.material3.*
import androidx.compose.runtime.*
import androidx.compose.runtime.saveable.rememberSaveable
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.res.painterResource
import androidx.compose.ui.semantics.Role
import androidx.compose.ui.text.input.KeyboardType
import androidx.compose.ui.text.input.PasswordVisualTransformation
import androidx.compose.ui.unit.dp
import com.petmedsolidario.R
import com.petmedsolidario.data.*
import java.time.LocalDate
import java.time.format.DateTimeFormatter

@Composable
fun Login(onEnter: (isPartner: Boolean, userName: String, userEmail: String) -> Unit) {
    var isPartner by rememberSaveable { mutableStateOf(false) }
    var isRegisterMode by rememberSaveable { mutableStateOf(false) }
    var name by rememberSaveable { mutableStateOf("") }
    var email by rememberSaveable { mutableStateOf("") }
    var password by rememberSaveable { mutableStateOf("") }
    var errorMsg by rememberSaveable { mutableStateOf("") }

    Page("PetMed Solidário") {
        Image(
            painterResource(R.drawable.petmed),
            contentDescription = null,
            modifier = Modifier.size(80.dp).align(Alignment.CenterHorizontally)
        )
        Panel {
            Text("Cuidar também é compartilhar.", style = MaterialTheme.typography.headlineSmall)
            Text("Selecione seu tipo de acesso e informe suas credenciais.")
        }

        // Seletor de Perfil: Tutor vs Parceiro
        Row(
            modifier = Modifier.fillMaxWidth(),
            horizontalArrangement = Arrangement.spacedBy(8.dp)
        ) {
            FilterChip(
                selected = !isPartner,
                onClick = { isPartner = false; errorMsg = "" },
                label = { Text("Sou Tutor") },
                modifier = Modifier.weight(1f)
            )
            FilterChip(
                selected = isPartner,
                onClick = { isPartner = true; errorMsg = "" },
                label = { Text("Sou Parceiro / Clínica") },
                modifier = Modifier.weight(1f)
            )
        }

        if (isRegisterMode) {
            OutlinedTextField(
                value = name,
                onValueChange = { name = it; errorMsg = "" },
                modifier = Modifier.fillMaxWidth(),
                label = { Text(if (isPartner) "Nome da Clínica / Parceiro" else "Seu nome completo") },
                singleLine = true
            )
        }

        OutlinedTextField(
            value = email,
            onValueChange = { email = it; errorMsg = "" },
            modifier = Modifier.fillMaxWidth(),
            label = { Text("E-mail") },
            placeholder = { Text(if (isPartner) "contato@clinica.com" else "seuemail@exemplo.com") },
            keyboardOptions = KeyboardOptions(keyboardType = KeyboardType.Email),
            singleLine = true
        )

        OutlinedTextField(
            value = password,
            onValueChange = { password = it; errorMsg = "" },
            modifier = Modifier.fillMaxWidth(),
            label = { Text("Senha") },
            visualTransformation = PasswordVisualTransformation(),
            keyboardOptions = KeyboardOptions(keyboardType = KeyboardType.Password),
            singleLine = true,
            supportingText = { Text("Mínimo de 4 caracteres.") }
        )

        if (errorMsg.isNotEmpty()) {
            Text(errorMsg, color = MaterialTheme.colorScheme.error, style = MaterialTheme.typography.bodySmall)
        }

        Action(if (isRegisterMode) "Criar conta e entrar" else "Entrar") {
            val cleanEmail = email.trim()
            if (!cleanEmail.contains("@") || cleanEmail.length < 5) {
                errorMsg = "Informe um e-mail válido."
            } else if (password.length < 4) {
                errorMsg = "A senha deve ter pelo menos 4 caracteres."
            } else if (isRegisterMode && name.isBlank()) {
                errorMsg = "Preencha o nome para concluir o cadastro."
            } else {
                val resolvedName = if (name.isNotBlank()) name.trim() else {
                    if (isPartner) "Equipe Parceira" else cleanEmail.substringBefore("@").replaceFirstChar { it.uppercase() }
                }
                onEnter(isPartner, resolvedName, cleanEmail)
            }
        }

        Row(
            modifier = Modifier.fillMaxWidth(),
            horizontalArrangement = Arrangement.SpaceBetween,
            verticalAlignment = Alignment.CenterVertically
        ) {
            TextButton(onClick = { isRegisterMode = !isRegisterMode; errorMsg = "" }) {
                Text(if (isRegisterMode) "Já tenho conta · Fazer login" else "Não tem conta? Cadastre-se")
            }
            TextButton(onClick = {
                if (isPartner) {
                    onEnter(true, "Equipe Ipê", "contato@clinica-ipe.com.br")
                } else {
                    onEnter(false, "Marina Silva", "marina@email.com")
                }
            }) {
                Text("Acesso rápido")
            }
        }
    }
}

@Composable
fun Search(
    catalog: List<Medicine>,
    points: List<Point>,
    available: (Medicine) -> Int,
    open: (Medicine) -> Unit
) {
    var query by rememberSaveable { mutableStateOf("") }
    var district by rememberSaveable { mutableStateOf("") }
    val districts = points.map { it.district }.distinct()

    Page("Buscar medicamentos") {
        OutlinedTextField(
            value = query,
            onValueChange = { query = it },
            modifier = Modifier.fillMaxWidth(),
            singleLine = true,
            label = { Text("Nome ou apresentação") }
        )
        Row(horizontalArrangement = Arrangement.spacedBy(8.dp)) {
            districts.forEach { d ->
                FilterChip(
                    selected = district == d,
                    onClick = { district = if (district == d) "" else d },
                    label = { Text(d) }
                )
            }
        }
        val matches = catalog.filter {
            available(it) > 0 && (district.isEmpty() || it.point.district == district) &&
                "${it.name} ${it.form}".contains(query.trim(), ignoreCase = true)
        }
        if (matches.isEmpty()) Text("Nenhum item encontrado. Tente outro nome ou remova o filtro de bairro.")
        matches.forEach { MedicineCard(it, available(it)) { open(it) } }
    }
}

/**
 * Aplica máscara brasileira DD/MM/AAAA conforme a digitação de números.
 */
fun formatDateBR(digitsOnly: String): String {
    val clean = digitsOnly.filter { it.isDigit() }.take(8)
    return buildString {
        for (i in clean.indices) {
            append(clean[i])
            if ((i == 1 || i == 3) && i != clean.lastIndex) {
                append('/')
            }
        }
    }
}

@Composable
fun Donation(
    points: List<Point>,
    onSubmit: (name: String, details: String) -> Unit
) {
    var name by rememberSaveable { mutableStateOf("") }
    var units by rememberSaveable { mutableStateOf("") }
    var expiresInput by rememberSaveable { mutableStateOf("") }
    var point by rememberSaveable { mutableStateOf(points.firstOrNull()?.name ?: "") }
    var accepted by rememberSaveable { mutableStateOf(false) }
    var review by rememberSaveable { mutableStateOf(false) }

    LaunchedEffect(points) {
        if (point.isEmpty() && points.isNotEmpty()) {
            point = points.first().name
        }
    }

    val parsedDate = if (expiresInput.length == 10) {
        runCatching {
            val formatter = DateTimeFormatter.ofPattern("dd/MM/yyyy")
            LocalDate.parse(expiresInput, formatter)
        }.getOrNull()
    } else null

    val isDateValid = parsedDate != null && parsedDate.isAfter(LocalDate.now())
    val valid = name.isNotBlank() && (units.toIntOrNull() ?: 0) > 0 && isDateValid && accepted && point.isNotBlank()

    Page(if (review) "Revisar doação" else "Cadastrar doação") {
        if (review) {
            Panel {
                Text(name, style = MaterialTheme.typography.titleLarge)
                Text("$units un. · Validade $expiresInput")
                Text("Ponto de entrega: $point")
                Text("Sua doação ficará disponível em 'Atividades' enquanto aguarda entrega.")
            }
            Action("Confirmar doação", valid) {
                review = false
                onSubmit(name, "$units un. · Validade $expiresInput\nPonto: $point")
                name = ""
                units = ""
                expiresInput = ""
                accepted = false
            }
            TextButton({ review = false }) { Text("Editar informações") }
        } else {
            OutlinedTextField(
                value = name,
                onValueChange = { name = it.take(80) },
                modifier = Modifier.fillMaxWidth(),
                label = { Text("Nome do medicamento") },
                singleLine = true
            )
            OutlinedTextField(
                value = units,
                onValueChange = { units = it.filter(Char::isDigit).take(5) },
                modifier = Modifier.fillMaxWidth(),
                label = { Text("Quantidade de unidades") },
                keyboardOptions = KeyboardOptions(keyboardType = KeyboardType.Number),
                singleLine = true
            )
            OutlinedTextField(
                value = expiresInput,
                onValueChange = { raw ->
                    expiresInput = formatDateBR(raw)
                },
                modifier = Modifier.fillMaxWidth(),
                singleLine = true,
                label = { Text("Validade (DD/MM/AAAA)") },
                placeholder = { Text("Ex: 31/12/2026") },
                keyboardOptions = KeyboardOptions(keyboardType = KeyboardType.Number),
                isError = expiresInput.length == 10 && !isDateValid,
                supportingText = {
                    if (expiresInput.length == 10 && !isDateValid) {
                        Text("Data inválida ou não futura.", color = MaterialTheme.colorScheme.error)
                    } else {
                        Text("Apenas medicamentos com validade futura.")
                    }
                }
            )
            Text("Ponto de entrega", style = MaterialTheme.typography.titleMedium)
            points.forEach {
                FilterChip(point == it.name, { point = it.name }, label = { Text("${it.name} · ${it.district}") })
            }
            Row(
                Modifier.fillMaxWidth().toggleable(accepted, role = Role.Checkbox, onValueChange = { accepted = it }).padding(vertical = 12.dp),
                verticalAlignment = Alignment.CenterVertically
            ) {
                Checkbox(accepted, null)
                Spacer(Modifier.width(8.dp))
                Text("Confirmo a conservação e integridade do medicamento.")
            }
            Action("Revisar doação", valid) { review = true }
        }
    }
}
