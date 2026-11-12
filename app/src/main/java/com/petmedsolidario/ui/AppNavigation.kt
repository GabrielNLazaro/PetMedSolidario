package com.petmedsolidario.ui

import androidx.compose.foundation.background
import androidx.compose.foundation.layout.*
import androidx.compose.foundation.text.KeyboardOptions
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.filled.*
import androidx.compose.material3.*
import androidx.compose.runtime.*
import androidx.compose.runtime.saveable.rememberSaveable
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.graphics.vector.ImageVector
import androidx.compose.ui.text.input.KeyboardType
import androidx.compose.ui.unit.dp
import androidx.navigation.compose.*
import com.petmedsolidario.data.*
import java.util.UUID

private data class Tab(val route: String, val label: String, val icon: ImageVector)
private val tutorTabs = listOf(
    Tab("home", "Início", Icons.Default.Home),
    Tab("search", "Buscar", Icons.Default.Search),
    Tab("donate", "Doar", Icons.Default.Add),
    Tab("activity", "Atividades", Icons.Default.List),
    Tab("profile", "Perfil", Icons.Default.Person)
)

private val partnerTabs = listOf(
    Tab("home", "Início", Icons.Default.Home),
    Tab("partner", "Gestão", Icons.Default.Build),
    Tab("search", "Catálogo", Icons.Default.Search),
    Tab("activity", "Atividades", Icons.Default.List),
    Tab("profile", "Perfil", Icons.Default.Person)
)

@Composable
fun PetMedApp() {
    var role by rememberSaveable { mutableStateOf<String?>(null) }
    var userName by rememberSaveable { mutableStateOf("") }
    var userEmail by rememberSaveable { mutableStateOf("") }

    if (role == null) {
        Surface(Modifier.fillMaxSize(), color = MaterialTheme.colorScheme.background) {
            Box(Modifier.safeDrawingPadding()) {
                Login { isPartner, name, email ->
                    role = if (isPartner) "Parceiro" else "Tutor"
                    userName = name
                    userEmail = email
                }
            }
        }
    } else {
        Session(
            partner = role == "Parceiro",
            userName = userName,
            userEmail = userEmail,
            logout = { role = null }
        )
    }
}

@Composable
private fun Session(
    partner: Boolean,
    userName: String,
    userEmail: String,
    logout: () -> Unit
) {
    val nav = rememberNavController()
    val entry by nav.currentBackStackEntryAsState()
    val route = entry?.destination?.route
    val currentTabs = if (partner) partnerTabs else tutorTabs

    // Estados dinâmicos para permitir CRUD de Parceiros e Medicamentos
    var pointsList by remember { mutableStateOf(initialPoints) }
    var catalogList by remember { mutableStateOf(initialCatalog) }

    // Lista de IDs de medicamentos reservados
    var reservations by rememberSaveable { mutableStateOf(arrayListOf<String>()) }
    // Lista estruturada de atividades (reservas e doações)
    var activities by remember { mutableStateOf(arrayListOf<UserActivity>()) }

    fun available(item: Medicine) = (item.units - reservations.count { it == item.id }).coerceAtLeast(0)

    fun cancelActivity(activity: UserActivity) {
        if (activity.type == ActivityType.RESERVATION && activity.medicineId != null) {
            val updatedReservations = ArrayList(reservations)
            val index = updatedReservations.indexOf(activity.medicineId)
            if (index != -1) {
                updatedReservations.removeAt(index)
                reservations = updatedReservations
            }
        }
        activities = ArrayList(activities.filter { it.id != activity.id })
    }

    fun goTab(target: String) {
        nav.navigate(target) {
            popUpTo("home") { saveState = true }
            launchSingleTop = true
            restoreState = true
        }
    }

    fun open(item: Medicine) {
        nav.navigate("medicine/${item.id}") { launchSingleTop = true }
    }

    Scaffold(
        topBar = {
            Surface(color = MaterialTheme.colorScheme.surface) {
                Row(
                    Modifier.fillMaxWidth().statusBarsPadding().heightIn(min = 56.dp).padding(horizontal = 16.dp),
                    verticalAlignment = Alignment.CenterVertically,
                    horizontalArrangement = Arrangement.SpaceBetween
                ) {
                    if (route != null && currentTabs.none { it.route == route }) {
                        TextButton({ nav.popBackStack() }) { Text("‹  Voltar") }
                    } else {
                        Text("PetMed Solidário", color = MaterialTheme.colorScheme.primary, style = MaterialTheme.typography.titleMedium)
                    }
                    Text(
                        if (partner) "Parceiro" else "Tutor",
                        Modifier.background(MaterialTheme.colorScheme.primaryContainer, MaterialTheme.shapes.small)
                            .padding(horizontal = 10.dp, vertical = 4.dp),
                        style = MaterialTheme.typography.labelMedium
                    )
                }
            }
        },
        bottomBar = {
            if (currentTabs.any { it.route == route }) NavigationBar {
                currentTabs.forEach { tab ->
                    NavigationBarItem(
                        selected = route == tab.route,
                        onClick = { if (route != tab.route) goTab(tab.route) },
                        icon = { Icon(tab.icon, null) },
                        label = { Text(tab.label) }
                    )
                }
            }
        }
    ) { padding ->
        NavHost(nav, startDestination = "home", modifier = Modifier.padding(padding).consumeWindowInsets(padding)) {
            composable("home") {
                Page("Olá, $userName") {
                    if (partner) {
                        Surface(shape = MaterialTheme.shapes.large, color = MaterialTheme.colorScheme.primaryContainer) {
                            Column(Modifier.fillMaxWidth().padding(20.dp), verticalArrangement = Arrangement.spacedBy(12.dp)) {
                                Text("Painel Administrativo do Parceiro", style = MaterialTheme.typography.headlineSmall)
                                Text("Gerencie pontos de coleta, cadastre medicamentos, edite quantidades ou remova itens.")
                                Action("Acessar Gestão do Parceiro") { goTab("partner") }
                            }
                        }
                    } else {
                        Surface(shape = MaterialTheme.shapes.large, color = MaterialTheme.colorScheme.primaryContainer) {
                            Column(Modifier.fillMaxWidth().padding(20.dp), verticalArrangement = Arrangement.spacedBy(12.dp)) {
                                Text("Cuidar também é compartilhar.", style = MaterialTheme.typography.headlineSmall)
                                Action("Buscar medicamentos") { goTab("search") }
                            }
                        }
                        OutlinedButton({ goTab("donate") }, Modifier.fillMaxWidth()) { Text("Fazer uma doação") }
                    }

                    Text("Disponíveis na região", style = MaterialTheme.typography.titleLarge)
                    val items = catalogList.filter { available(it) > 0 }
                    if (items.isEmpty()) Text("Nenhum medicamento disponível no momento.")
                    items.take(3).forEach { MedicineCard(it, available(it)) { open(it) } }
                    TextButton({ nav.navigate("points") }) { Text("Ver pontos parceiros (${pointsList.size})") }
                }
            }
            composable("search") {
                Search(catalogList, pointsList, ::available, ::open)
            }
            composable("medicine/{id}") { screen ->
                val medId = screen.arguments?.getString("id")
                val item = catalogList.firstOrNull { it.id == medId }
                if (item == null) {
                    Page("Medicamento não encontrado") {
                        Text("Este item foi removido do catálogo.")
                        Action("Voltar") { nav.popBackStack() }
                    }
                } else {
                    Page(item.name) {
                        Panel {
                            Text("${item.form} · ${item.strength}", style = MaterialTheme.typography.titleLarge)
                            Text("Lote ${item.id.uppercase()} · Validade 12/2026")
                            Text("${available(item)} unidades disponíveis para retirada")
                        }
                        Panel {
                            Text(item.point.name, style = MaterialTheme.typography.titleMedium)
                            Text(item.point.address)
                            Text(item.point.hours)
                        }
                        Action("Reservar 1 unidade", available(item) > 0) {
                            if (available(item) > 0 && nav.currentDestination?.route == "medicine/{id}") {
                                reservations = ArrayList(reservations + item.id)
                                val newActivity = UserActivity(
                                    id = UUID.randomUUID().toString(),
                                    type = ActivityType.RESERVATION,
                                    title = "Reserva · 1 un. de ${item.name}",
                                    details = "Retirar em: ${item.point.name} (${item.point.address})",
                                    medicineId = item.id
                                )
                                activities = ArrayList(listOf(newActivity) + activities)
                                nav.navigate("receipt/${item.id}")
                            }
                        }
                    }
                }
            }
            composable("receipt/{id}") { screen ->
                val item = catalogList.firstOrNull { it.id == screen.arguments?.getString("id") }
                Page("Reserva confirmada!") {
                    if (item != null) {
                        Panel {
                            Text(item.name, style = MaterialTheme.typography.titleLarge)
                            Text("1 unidade reservada em ${item.point.name}")
                            Text("Apresente o comprovante no local dentro do horário de atendimento para retirar.")
                        }
                    }
                    Action("Ver minhas atividades") {
                        nav.popBackStack("home", false)
                        goTab("activity")
                    }
                }
            }
            composable("donate") {
                Donation(pointsList) { medName, details ->
                    val newActivity = UserActivity(
                        id = UUID.randomUUID().toString(),
                        type = ActivityType.DONATION,
                        title = "Doação cadastrada · $medName",
                        details = "$details\nStatus: Aguardando entrega no ponto parceiro",
                        medicineId = null
                    )
                    activities = ArrayList(listOf(newActivity) + activities)
                    goTab("activity")
                }
            }
            composable("activity") {
                Page("Minhas atividades") {
                    if (activities.isEmpty()) {
                        Panel {
                            Text("Você ainda não possui doações ou reservas registradas.")
                        }
                    }
                    activities.forEach { item ->
                        ElevatedCard(
                            modifier = Modifier.fillMaxWidth(),
                            shape = MaterialTheme.shapes.medium,
                            colors = CardDefaults.elevatedCardColors(containerColor = MaterialTheme.colorScheme.surface)
                        ) {
                            Column(Modifier.padding(16.dp), verticalArrangement = Arrangement.spacedBy(8.dp)) {
                                val badgeText = if (item.type == ActivityType.RESERVATION) "RESERVA" else "DOAÇÃO"
                                Text(
                                    badgeText,
                                    style = MaterialTheme.typography.labelSmall,
                                    color = MaterialTheme.colorScheme.primary
                                )
                                Text(item.title, style = MaterialTheme.typography.titleMedium)
                                Text(item.details, style = MaterialTheme.typography.bodyMedium)

                                Row(
                                    modifier = Modifier.fillMaxWidth(),
                                    horizontalArrangement = Arrangement.End
                                ) {
                                    OutlinedButton(
                                        onClick = { cancelActivity(item) },
                                        colors = ButtonDefaults.outlinedButtonColors(
                                            contentColor = MaterialTheme.colorScheme.error
                                        )
                                    ) {
                                        Icon(
                                            Icons.Default.Delete,
                                            contentDescription = null,
                                            modifier = Modifier.size(18.dp)
                                        )
                                        Spacer(Modifier.width(6.dp))
                                        Text(
                                            if (item.type == ActivityType.RESERVATION) "Cancelar reserva" else "Excluir doação"
                                        )
                                    }
                                }
                            }
                        }
                    }
                }
            }
            composable("profile") {
                Page("Meu perfil") {
                    Panel {
                        Text(userName, style = MaterialTheme.typography.titleLarge)
                        Text(userEmail, style = MaterialTheme.typography.bodyMedium)
                        Text(
                            if (partner) "Perfil: Parceiro / Administrador" else "Perfil: Tutor(a)",
                            color = MaterialTheme.colorScheme.primary,
                            style = MaterialTheme.typography.labelLarge
                        )
                    }
                    if (partner) {
                        Action("Abrir Gestão do Parceiro") { goTab("partner") }
                    }
                    OutlinedButton(onClick = logout, modifier = Modifier.fillMaxWidth()) {
                        Text("Sair da conta")
                    }
                }
            }
            composable("points") {
                Page("Pontos parceiros") {
                    if (pointsList.isEmpty()) {
                        Panel { Text("Nenhum ponto parceiro cadastrado no momento.") }
                    }
                    pointsList.forEach {
                        Panel {
                            Text(it.name, style = MaterialTheme.typography.titleLarge)
                            Text("${it.address} · ${it.district}")
                            Text(it.hours)
                        }
                    }
                }
            }
            composable("partner") {
                PartnerManagementScreen(
                    points = pointsList,
                    catalog = catalogList,
                    available = ::available,
                    onSavePoint = { savedPoint ->
                        val exists = pointsList.any { it.id == savedPoint.id }
                        pointsList = if (exists) {
                            pointsList.map { if (it.id == savedPoint.id) savedPoint else it }
                        } else {
                            pointsList + savedPoint
                        }
                        // Atualiza referências de ponto nos medicamentos
                        catalogList = catalogList.map { med ->
                            if (med.point.id == savedPoint.id) med.copy(point = savedPoint) else med
                        }
                    },
                    onDeletePoint = { pointToDelete ->
                        pointsList = pointsList.filter { it.id != pointToDelete.id }
                        catalogList = catalogList.filter { it.point.id != pointToDelete.id }
                    },
                    onSaveMedicine = { savedMed ->
                        val exists = catalogList.any { it.id == savedMed.id }
                        catalogList = if (exists) {
                            catalogList.map { if (it.id == savedMed.id) savedMed else it }
                        } else {
                            catalogList + savedMed
                        }
                    },
                    onDeleteMedicine = { medToDelete ->
                        catalogList = catalogList.filter { it.id != medToDelete.id }
                    }
                )
            }
        }
    }
}

@Composable
private fun PartnerManagementScreen(
    points: List<Point>,
    catalog: List<Medicine>,
    available: (Medicine) -> Int,
    onSavePoint: (Point) -> Unit,
    onDeletePoint: (Point) -> Unit,
    onSaveMedicine: (Medicine) -> Unit,
    onDeleteMedicine: (Medicine) -> Unit
) {
    var selectedSection by rememberSaveable { mutableStateOf(0) } // 0 = Parceiros, 1 = Estoque

    // Estados de formulário de Parceiro (Point)
    var showPointForm by rememberSaveable { mutableStateOf(false) }
    var editingPointId by rememberSaveable { mutableStateOf<String?>(null) }
    var pName by rememberSaveable { mutableStateOf("") }
    var pDistrict by rememberSaveable { mutableStateOf("") }
    var pAddress by rememberSaveable { mutableStateOf("") }
    var pHours by rememberSaveable { mutableStateOf("") }

    // Estados de formulário de Medicamento (Medicine)
    var showMedForm by rememberSaveable { mutableStateOf(false) }
    var editingMedId by rememberSaveable { mutableStateOf<String?>(null) }
    var mName by rememberSaveable { mutableStateOf("") }
    var mForm by rememberSaveable { mutableStateOf("") }
    var mStrength by rememberSaveable { mutableStateOf("") }
    var mUnits by rememberSaveable { mutableStateOf("") }
    var mPointId by rememberSaveable { mutableStateOf("") }

    fun openNewPointForm() {
        editingPointId = null
        pName = ""
        pDistrict = ""
        pAddress = ""
        pHours = ""
        showPointForm = true
    }

    fun openEditPointForm(point: Point) {
        editingPointId = point.id
        pName = point.name
        pDistrict = point.district
        pAddress = point.address
        pHours = point.hours
        showPointForm = true
    }

    fun openNewMedForm() {
        editingMedId = null
        mName = ""
        mForm = "Comprimidos"
        mStrength = ""
        mUnits = ""
        mPointId = points.firstOrNull()?.id ?: ""
        showMedForm = true
    }

    fun openEditMedForm(med: Medicine) {
        editingMedId = med.id
        mName = med.name
        mForm = med.form
        mStrength = med.strength
        mUnits = med.units.toString()
        mPointId = med.point.id
        showMedForm = true
    }

    Page("Painel do Parceiro") {
        // Abas internas: Parceiros vs Estoque
        Row(
            modifier = Modifier.fillMaxWidth(),
            horizontalArrangement = Arrangement.spacedBy(8.dp)
        ) {
            FilterChip(
                selected = selectedSection == 0,
                onClick = { selectedSection = 0; showMedForm = false },
                label = { Text("Pontos Parceiros (${points.size})") },
                modifier = Modifier.weight(1f)
            )
            FilterChip(
                selected = selectedSection == 1,
                onClick = { selectedSection = 1; showPointForm = false },
                label = { Text("Estoque (${catalog.size})") },
                modifier = Modifier.weight(1f)
            )
        }

        if (selectedSection == 0) {
            // SEÇÃO 1: CRUD DE PARCEIROS (CLÍNICAS / PONTOS DE COLETA)
            if (showPointForm) {
                Panel {
                    Text(
                        if (editingPointId == null) "Novo Ponto Parceiro" else "Editar Ponto Parceiro",
                        style = MaterialTheme.typography.titleLarge
                    )
                    OutlinedTextField(
                        value = pName,
                        onValueChange = { pName = it },
                        label = { Text("Nome da Clínica / ONG") },
                        modifier = Modifier.fillMaxWidth(),
                        singleLine = true
                    )
                    OutlinedTextField(
                        value = pDistrict,
                        onValueChange = { pDistrict = it },
                        label = { Text("Bairro") },
                        modifier = Modifier.fillMaxWidth(),
                        singleLine = true
                    )
                    OutlinedTextField(
                        value = pAddress,
                        onValueChange = { pAddress = it },
                        label = { Text("Endereço completo") },
                        modifier = Modifier.fillMaxWidth(),
                        singleLine = true
                    )
                    OutlinedTextField(
                        value = pHours,
                        onValueChange = { pHours = it },
                        label = { Text("Horário de funcionamento") },
                        placeholder = { Text("Ex: Seg a Sex, 8h às 18h") },
                        modifier = Modifier.fillMaxWidth(),
                        singleLine = true
                    )
                    val validPoint = pName.isNotBlank() && pDistrict.isNotBlank() && pAddress.isNotBlank() && pHours.isNotBlank()
                    Action(if (editingPointId == null) "Salvar Parceiro" else "Atualizar Parceiro", validPoint) {
                        val pointObj = Point(
                            id = editingPointId ?: "p_${UUID.randomUUID().toString().take(6)}",
                            name = pName.trim(),
                            district = pDistrict.trim(),
                            address = pAddress.trim(),
                            hours = pHours.trim()
                        )
                        onSavePoint(pointObj)
                        showPointForm = false
                    }
                    TextButton(onClick = { showPointForm = false }, modifier = Modifier.fillMaxWidth()) {
                        Text("Cancelar")
                    }
                }
            } else {
                Action("+ Cadastrar Novo Parceiro") { openNewPointForm() }
            }

            if (points.isEmpty()) {
                Panel { Text("Nenhum parceiro cadastrado. Clique acima para adicionar.") }
            }

            points.forEach { pt ->
                ElevatedCard(
                    modifier = Modifier.fillMaxWidth(),
                    shape = MaterialTheme.shapes.medium,
                    colors = CardDefaults.elevatedCardColors(containerColor = MaterialTheme.colorScheme.surface)
                ) {
                    Column(Modifier.padding(16.dp), verticalArrangement = Arrangement.spacedBy(6.dp)) {
                        Text(pt.name, style = MaterialTheme.typography.titleLarge)
                        Text("${pt.address} · ${pt.district}")
                        Text(pt.hours, style = MaterialTheme.typography.bodySmall)
                        Row(
                            modifier = Modifier.fillMaxWidth().padding(top = 8.dp),
                            horizontalArrangement = Arrangement.spacedBy(8.dp, Alignment.End)
                        ) {
                            OutlinedButton(onClick = { openEditPointForm(pt) }) {
                                Icon(Icons.Default.Edit, contentDescription = null, modifier = Modifier.size(16.dp))
                                Spacer(Modifier.width(4.dp))
                                Text("Editar")
                            }
                            OutlinedButton(
                                onClick = { onDeletePoint(pt) },
                                colors = ButtonDefaults.outlinedButtonColors(contentColor = MaterialTheme.colorScheme.error)
                            ) {
                                Icon(Icons.Default.Delete, contentDescription = null, modifier = Modifier.size(16.dp))
                                Spacer(Modifier.width(4.dp))
                                Text("Remover")
                            }
                        }
                    }
                }
            }
        } else {
            // SEÇÃO 2: CRUD DE MEDICAMENTOS DO ESTOQUE
            if (points.isEmpty()) {
                Panel {
                    Text("Cadastre pelo menos um Ponto Parceiro antes de adicionar medicamentos ao estoque.")
                }
            } else if (showMedForm) {
                Panel {
                    Text(
                        if (editingMedId == null) "Cadastrar Medicamento" else "Editar Medicamento",
                        style = MaterialTheme.typography.titleLarge
                    )
                    OutlinedTextField(
                        value = mName,
                        onValueChange = { mName = it },
                        label = { Text("Nome do medicamento") },
                        modifier = Modifier.fillMaxWidth(),
                        singleLine = true
                    )
                    OutlinedTextField(
                        value = mForm,
                        onValueChange = { mForm = it },
                        label = { Text("Apresentação (Comprimidos, Solução, etc.)") },
                        modifier = Modifier.fillMaxWidth(),
                        singleLine = true
                    )
                    OutlinedTextField(
                        value = mStrength,
                        onValueChange = { mStrength = it },
                        label = { Text("Dosagem / Concentração (ex: 10 mg)") },
                        modifier = Modifier.fillMaxWidth(),
                        singleLine = true
                    )
                    OutlinedTextField(
                        value = mUnits,
                        onValueChange = { mUnits = it.filter(Char::isDigit).take(5) },
                        label = { Text("Quantidade em estoque") },
                        keyboardOptions = KeyboardOptions(keyboardType = KeyboardType.Number),
                        modifier = Modifier.fillMaxWidth(),
                        singleLine = true
                    )
                    Text("Ponto parceiro responsável", style = MaterialTheme.typography.titleSmall)
                    points.forEach { pt ->
                        FilterChip(
                            selected = mPointId == pt.id,
                            onClick = { mPointId = pt.id },
                            label = { Text("${pt.name} (${pt.district})") }
                        )
                    }
                    val selectedPt = points.firstOrNull { it.id == mPointId } ?: points.first()
                    val validMed = mName.isNotBlank() && mForm.isNotBlank() && mStrength.isNotBlank() && (mUnits.toIntOrNull() ?: 0) > 0
                    Action(if (editingMedId == null) "Salvar Medicamento" else "Atualizar Medicamento", validMed) {
                        val medObj = Medicine(
                            id = editingMedId ?: "med_${UUID.randomUUID().toString().take(5)}",
                            name = mName.trim(),
                            form = mForm.trim(),
                            strength = mStrength.trim(),
                            units = mUnits.toInt(),
                            point = selectedPt
                        )
                        onSaveMedicine(medObj)
                        showMedForm = false
                    }
                    TextButton(onClick = { showMedForm = false }, modifier = Modifier.fillMaxWidth()) {
                        Text("Cancelar")
                    }
                }
            } else {
                Action("+ Cadastrar Medicamento no Estoque") { openNewMedForm() }
            }

            catalog.forEach { med ->
                ElevatedCard(
                    modifier = Modifier.fillMaxWidth(),
                    shape = MaterialTheme.shapes.medium,
                    colors = CardDefaults.elevatedCardColors(containerColor = MaterialTheme.colorScheme.surface)
                ) {
                    Column(Modifier.padding(16.dp), verticalArrangement = Arrangement.spacedBy(6.dp)) {
                        Text("${med.form.uppercase()} · ${med.strength}", style = MaterialTheme.typography.labelMedium, color = MaterialTheme.colorScheme.primary)
                        Text(med.name, style = MaterialTheme.typography.titleLarge)
                        Text("Local: ${med.point.name} (${med.point.district})")
                        Text("Disponível: ${available(med)} de ${med.units} un.", style = MaterialTheme.typography.labelLarge)
                        Row(
                            modifier = Modifier.fillMaxWidth().padding(top = 8.dp),
                            horizontalArrangement = Arrangement.spacedBy(8.dp, Alignment.End)
                        ) {
                            OutlinedButton(onClick = { openEditMedForm(med) }) {
                                Icon(Icons.Default.Edit, contentDescription = null, modifier = Modifier.size(16.dp))
                                Spacer(Modifier.width(4.dp))
                                Text("Editar")
                            }
                            OutlinedButton(
                                onClick = { onDeleteMedicine(med) },
                                colors = ButtonDefaults.outlinedButtonColors(contentColor = MaterialTheme.colorScheme.error)
                            ) {
                                Icon(Icons.Default.Delete, contentDescription = null, modifier = Modifier.size(16.dp))
                                Spacer(Modifier.width(4.dp))
                                Text("Remover")
                            }
                        }
                    }
                }
            }
        }
    }
}
