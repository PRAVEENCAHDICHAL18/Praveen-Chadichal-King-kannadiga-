package com.example

import android.os.Bundle
import androidx.activity.ComponentActivity
import androidx.activity.compose.setContent
import androidx.activity.enableEdgeToEdge
import androidx.compose.animation.*
import androidx.compose.foundation.*
import androidx.compose.foundation.layout.*
import androidx.compose.foundation.lazy.LazyColumn
import androidx.compose.foundation.lazy.items
import androidx.compose.foundation.shape.CircleShape
import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.foundation.text.KeyboardOptions
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.filled.*
import androidx.compose.material.icons.outlined.*
import androidx.compose.material3.*
import androidx.compose.runtime.*
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.draw.clip
import androidx.compose.ui.draw.shadow
import androidx.compose.ui.graphics.Brush
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.platform.LocalContext
import androidx.compose.ui.platform.testTag
import androidx.compose.ui.res.stringResource
import androidx.compose.ui.text.font.FontFamily
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.text.input.KeyboardType
import androidx.compose.ui.text.style.TextAlign
import androidx.compose.ui.text.style.TextOverflow
import androidx.compose.ui.unit.dp
import androidx.compose.ui.unit.sp
import androidx.lifecycle.compose.collectAsStateWithLifecycle
import androidx.lifecycle.viewmodel.compose.viewModel
import com.example.data.db.BloomDatabase
import com.example.data.db.CartItem
import com.example.data.db.Order
import com.example.data.model.Bouquet
import com.example.data.repository.BloomRepository
import com.example.ui.components.BouquetVisual
import com.example.ui.theme.MyApplicationTheme
import com.example.ui.viewmodel.BloomViewModel
import com.example.ui.viewmodel.BloomViewModelFactory
import kotlinx.coroutines.delay
import java.util.Calendar

class MainActivity : ComponentActivity() {
    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        enableEdgeToEdge()
        setContent {
            MyApplicationTheme {
                val context = LocalContext.current
                val database = remember { BloomDatabase.getDatabase(context.applicationContext) }
                val repository = remember { BloomRepository(database.cartDao(), database.orderDao()) }
                val viewModel: BloomViewModel = viewModel(factory = BloomViewModelFactory(repository))
                
                MainScreen(viewModel)
            }
        }
    }
}

@Composable
fun FrostedGlassBackground() {
    val isDark = isSystemInDarkTheme()
    val pinkColor = if (isDark) Color(0xFF6A1A31).copy(alpha = 0.35f) else Color(0xFFFFD2E1).copy(alpha = 0.55f)
    val emeraldColor = if (isDark) Color(0xFF0F3A2A).copy(alpha = 0.35f) else Color(0xFFD3F2E4).copy(alpha = 0.5f)
    val orchidColor = if (isDark) Color(0xFF381440).copy(alpha = 0.35f) else Color(0xFFFFE3E8).copy(alpha = 0.45f)
    
    Box(
        modifier = Modifier
            .fillMaxSize()
            .background(MaterialTheme.colorScheme.background)
    ) {
        // Upper left pinkish blob
        Box(
            modifier = Modifier
                .offset(x = (-80).dp, y = (-80).dp)
                .size(280.dp)
                .background(
                    brush = Brush.radialGradient(
                        colors = listOf(
                            pinkColor,
                            pinkColor.copy(alpha = 0.4f),
                            Color.Transparent
                        )
                    ),
                    shape = CircleShape
                )
        )
        // Mid-right emerald/mint blob
        Box(
            modifier = Modifier
                .align(Alignment.CenterEnd)
                .offset(x = 100.dp, y = 50.dp)
                .size(320.dp)
                .background(
                    brush = Brush.radialGradient(
                        colors = listOf(
                            emeraldColor,
                            emeraldColor.copy(alpha = 0.4f),
                            Color.Transparent
                        )
                    ),
                    shape = CircleShape
                )
        )
        
        // Lower left bottom-mid soft rose/emerald blend blob
        Box(
            modifier = Modifier
                .align(Alignment.BottomStart)
                .offset(x = (-50).dp, y = 100.dp)
                .size(300.dp)
                .background(
                    brush = Brush.radialGradient(
                        colors = listOf(
                            orchidColor,
                            orchidColor.copy(alpha = 0.4f),
                            Color.Transparent
                        )
                    ),
                    shape = CircleShape
                )
        )
    }
}

@OptIn(ExperimentalMaterial3Api::class)
@Composable
fun MainScreen(viewModel: BloomViewModel) {
    val currentTab by viewModel.currentTab.collectAsStateWithLifecycle()
    val cartItems by viewModel.cartItems.collectAsStateWithLifecycle()
    val cartCount = remember(cartItems) { cartItems.sumOf { it.quantity } }
    val isDark = isSystemInDarkTheme()

    Box(modifier = Modifier.fillMaxSize()) {
        FrostedGlassBackground()

        Scaffold(
            modifier = Modifier.fillMaxSize(),
            containerColor = Color.Transparent,
            topBar = {
                TopAppBar(
                    title = {
                        Row(
                            verticalAlignment = Alignment.CenterVertically,
                            horizontalArrangement = Arrangement.spacedBy(8.dp)
                        ) {
                            Icon(
                                imageVector = Icons.Default.LocalFlorist,
                                contentDescription = null,
                                tint = MaterialTheme.colorScheme.primary,
                                modifier = Modifier.size(30.dp)
                            )
                            Text(
                                text = "b l o o m",
                                fontWeight = FontWeight.ExtraBold,
                                fontFamily = FontFamily.SansSerif,
                                fontSize = 22.sp,
                                color = MaterialTheme.colorScheme.primary
                            )
                        }
                    },
                    colors = TopAppBarDefaults.topAppBarColors(
                        containerColor = if (isDark) Color(0x3310080B) else Color(0x99FDF7F9),
                        titleContentColor = MaterialTheme.colorScheme.primary
                    ),
                    modifier = Modifier.border(
                        BorderStroke(0.5.dp, Color.White.copy(alpha = if (isDark) 0.1f else 0.4f))
                    ),
                    actions = {
                        // Small info badge
                        IconButton(onClick = {}) {
                            Icon(
                                imageVector = Icons.Default.Info,
                                contentDescription = "Express Shipping Info",
                                tint = MaterialTheme.colorScheme.primary.copy(alpha = 0.7f)
                            )
                        }
                    }
                )
            },
            bottomBar = {
                NavigationBar(
                    containerColor = if (isDark) Color(0x3310080B) else Color(0x99FDF7F9),
                    windowInsets = WindowInsets.navigationBars,
                    modifier = Modifier.border(
                        BorderStroke(0.5.dp, Color.White.copy(alpha = if (isDark) 0.1f else 0.4f))
                    )
                ) {
                    NavigationBarItem(
                        selected = currentTab == 0,
                        onClick = { viewModel.selectTab(0) },
                        icon = { Icon(Icons.Default.Storefront, contentDescription = "Bouquet Shop") },
                        label = { Text("Shop", fontWeight = FontWeight.Bold) },
                        colors = NavigationBarItemDefaults.colors(
                            selectedIconColor = MaterialTheme.colorScheme.primary,
                            selectedTextColor = MaterialTheme.colorScheme.primary,
                            indicatorColor = if (isDark) Color(0x22FFFFFF) else Color(0x1CE63B66),
                            unselectedIconColor = MaterialTheme.colorScheme.onBackground.copy(alpha = 0.6f),
                            unselectedTextColor = MaterialTheme.colorScheme.onBackground.copy(alpha = 0.6f)
                        ),
                        modifier = Modifier.testTag("shop_navigation_tab")
                    )
                    NavigationBarItem(
                        selected = currentTab == 1,
                        onClick = { viewModel.selectTab(1) },
                        icon = { Icon(Icons.Default.AutoAwesome, contentDescription = "AI Custom Bouquets") },
                        label = { Text("AI Creator", fontWeight = FontWeight.Bold) },
                        colors = NavigationBarItemDefaults.colors(
                            selectedIconColor = MaterialTheme.colorScheme.primary,
                            selectedTextColor = MaterialTheme.colorScheme.primary,
                            indicatorColor = if (isDark) Color(0x22FFFFFF) else Color(0x1CE63B66),
                            unselectedIconColor = MaterialTheme.colorScheme.onBackground.copy(alpha = 0.6f),
                            unselectedTextColor = MaterialTheme.colorScheme.onBackground.copy(alpha = 0.6f)
                        ),
                        modifier = Modifier.testTag("creator_navigation_tab")
                    )
                    NavigationBarItem(
                        selected = currentTab == 2,
                        onClick = { viewModel.selectTab(2) },
                        icon = {
                            BadgedBox(
                                badge = {
                                    if (cartCount > 0) {
                                        Badge(
                                            containerColor = MaterialTheme.colorScheme.primary,
                                            contentColor = MaterialTheme.colorScheme.onPrimary
                                        ) {
                                            Text(cartCount.toString())
                                        }
                                    }
                                }
                            ) {
                                Icon(Icons.Default.ShoppingCart, contentDescription = "Shopping Cart")
                            }
                        },
                        label = { Text("Cart", fontWeight = FontWeight.Bold) },
                        colors = NavigationBarItemDefaults.colors(
                            selectedIconColor = MaterialTheme.colorScheme.primary,
                            selectedTextColor = MaterialTheme.colorScheme.primary,
                            indicatorColor = if (isDark) Color(0x22FFFFFF) else Color(0x1CE63B66),
                            unselectedIconColor = MaterialTheme.colorScheme.onBackground.copy(alpha = 0.6f),
                            unselectedTextColor = MaterialTheme.colorScheme.onBackground.copy(alpha = 0.6f)
                        ),
                        modifier = Modifier.testTag("cart_navigation_tab")
                    )
                    NavigationBarItem(
                        selected = currentTab == 3,
                        onClick = { viewModel.selectTab(3) },
                        icon = { Icon(Icons.Default.LocalShipping, contentDescription = "My Orders") },
                        label = { Text("Orders", fontWeight = FontWeight.Bold) },
                        colors = NavigationBarItemDefaults.colors(
                            selectedIconColor = MaterialTheme.colorScheme.primary,
                            selectedTextColor = MaterialTheme.colorScheme.primary,
                            indicatorColor = if (isDark) Color(0x22FFFFFF) else Color(0x1CE63B66),
                            unselectedIconColor = MaterialTheme.colorScheme.onBackground.copy(alpha = 0.6f),
                            unselectedTextColor = MaterialTheme.colorScheme.onBackground.copy(alpha = 0.6f)
                        ),
                        modifier = Modifier.testTag("orders_navigation_tab")
                    )
                }
            }
        ) { innerPadding ->
            Box(
                modifier = Modifier
                    .fillMaxSize()
                    .padding(innerPadding)
            ) {
                when (currentTab) {
                    0 -> ShopTab(viewModel)
                    1 -> CreatorTab(viewModel)
                    2 -> CartTab(viewModel)
                    3 -> OrdersTab(viewModel)
                }
            }
        }
    }
}

// ======================== TABS ========================

@Composable
fun ShopTab(viewModel: BloomViewModel) {
    val filteredBouquets by viewModel.filteredBouquets.collectAsStateWithLifecycle()
    val selectedOccasion by viewModel.selectedCatalogOccasion.collectAsStateWithLifecycle()
    
    // Live ticking countdown
    var countdownString by remember { mutableStateOf("00h 00m 00s remaining") }
    LaunchedEffect(Unit) {
        while (true) {
            val now = Calendar.getInstance()
            val cutoff = Calendar.getInstance().apply {
                set(Calendar.HOUR_OF_DAY, 17) // cutoff at 5 PM
                set(Calendar.MINUTE, 0)
                set(Calendar.SECOND, 0)
            }
            
            val diffMs = if (now.before(cutoff)) {
                cutoff.timeInMillis - now.timeInMillis
            } else {
                cutoff.add(Calendar.DAY_OF_YEAR, 1)
                cutoff.timeInMillis - now.timeInMillis
            }
            
            val hours = diffMs / (1000 * 60 * 60)
            val minutes = (diffMs / (1000 * 60)) % 60
            val seconds = (diffMs / 1000) % 60
            
            countdownString = String.format("%02dh %02dm %02ds remaining", hours, minutes, seconds)
            delay(1000)
        }
    }

    LazyColumn(
        modifier = Modifier
            .fillMaxSize()
            .testTag("shop_tab_list"),
        contentPadding = PaddingValues(16.dp),
        verticalArrangement = Arrangement.spacedBy(16.dp)
    ) {
        // Countdown banner
        item {
            val isDark = isSystemInDarkTheme()
            Card(
                colors = CardDefaults.cardColors(
                    containerColor = if (isDark) Color(0x35FFB4C1) else Color(0x22D63B66),
                    contentColor = if (isDark) Color(0xFFFFD5D9) else Color(0xFF8C1335)
                ),
                shape = RoundedCornerShape(24.dp),
                border = BorderStroke(1.dp, if (isDark) Color.White.copy(alpha = 0.15f) else Color.White.copy(alpha = 0.5f)),
                modifier = Modifier
                    .fillMaxWidth()
                    .shadow(1.dp, RoundedCornerShape(24.dp))
            ) {
                Row(
                    modifier = Modifier
                        .fillMaxWidth()
                        .padding(16.dp),
                    verticalAlignment = Alignment.CenterVertically,
                    horizontalArrangement = Arrangement.spacedBy(12.dp)
                ) {
                    Box(
                        modifier = Modifier
                            .size(48.dp)
                            .background(MaterialTheme.colorScheme.primary, CircleShape),
                        contentAlignment = Alignment.Center
                    ) {
                        Icon(
                            imageVector = Icons.Default.HourglassTop,
                            contentDescription = null,
                            tint = MaterialTheme.colorScheme.onPrimary,
                            modifier = Modifier.size(26.dp)
                        )
                    }
                    Column {
                        Text(
                            text = "Same-Day Courier Express Shipping",
                            fontWeight = FontWeight.Bold,
                            fontSize = 15.sp
                        )
                        Text(
                            text = "Order in $countdownString for guaranteed delivery today or choose drone shipping!",
                            fontSize = 12.sp,
                            lineHeight = 16.sp,
                            color = (if (isDark) Color(0xFFFFD5D9) else Color(0xFF8C1335)).copy(alpha = 0.8f)
                        )
                    }
                }
            }
        }

        // Section header with flower shop hero tagline
        item {
            Column(modifier = Modifier.padding(vertical = 8.dp)) {
                Text(
                    text = "Aromatic Living Bouquets",
                    style = MaterialTheme.typography.titleLarge,
                    fontWeight = FontWeight.Bold,
                    color = MaterialTheme.colorScheme.onBackground
                )
                Text(
                    text = "Select from our curated signature arrangements hand-tied fresh every morning.",
                    style = MaterialTheme.typography.bodyMedium,
                    color = MaterialTheme.colorScheme.onBackground.copy(alpha = 0.7f)
                )
            }
        }

        // Category filter chips
        item {
            Row(
                modifier = Modifier
                    .fillMaxWidth()
                    .horizontalScroll(rememberScrollState()),
                horizontalArrangement = Arrangement.spacedBy(8.dp)
            ) {
                val isDark = isSystemInDarkTheme()
                viewModel.occasions.forEach { occasion ->
                    FilterChip(
                        selected = selectedOccasion == occasion,
                        onClick = { viewModel.setCatalogOccasion(occasion) },
                        label = { Text(occasion, fontWeight = FontWeight.SemiBold) },
                        shape = RoundedCornerShape(16.dp),
                        colors = FilterChipDefaults.filterChipColors(
                            selectedContainerColor = MaterialTheme.colorScheme.primary,
                            selectedLabelColor = MaterialTheme.colorScheme.onPrimary,
                            containerColor = if (isDark) Color(0x22FFFFFF) else Color(0x66FFFFFF),
                            labelColor = MaterialTheme.colorScheme.onBackground.copy(alpha = 0.8f)
                        ),
                        border = FilterChipDefaults.filterChipBorder(
                            enabled = true,
                            selected = selectedOccasion == occasion,
                            borderColor = Color.White.copy(alpha = if (isDark) 0.15f else 0.5f),
                            selectedBorderColor = MaterialTheme.colorScheme.primary,
                            borderWidth = 1.dp,
                            selectedBorderWidth = 1.5.dp
                        ),
                        modifier = Modifier.testTag("filter_chip_$occasion")
                    )
                }
            }
        }

        // Bouquets list
        items(filteredBouquets, key = { it.id }) { bouquet ->
            BouquetCard(
                bouquet = bouquet,
                onAddClick = { viewModel.addBouquetToCart(bouquet) }
            )
        }
    }
}

@Composable
fun CreatorTab(viewModel: BloomViewModel) {
    val occasion by viewModel.creatorOccasion.collectAsStateWithLifecycle()
    val colorTheme by viewModel.creatorColor.collectAsStateWithLifecycle()
    val prompt by viewModel.creatorPrompt.collectAsStateWithLifecycle()
    val selectedStems by viewModel.selectedStems.collectAsStateWithLifecycle()
    
    val isGenerating by viewModel.isGenerating.collectAsStateWithLifecycle()
    val generationError by viewModel.generationError.collectAsStateWithLifecycle()
    val generatedBouquet by viewModel.generatedBouquet.collectAsStateWithLifecycle()

    val isDark = isSystemInDarkTheme()

    data class ColorPaletteData(
        val name: String,
        val description: String,
        val colors: List<Color>
    )

    val palettes = remember {
        listOf(
            ColorPaletteData("Pastel Dream & White", "Soft cream, blush pink, and mint accents.", listOf(Color(0xFFFFD2E1), Color(0xFFF9F5F0), Color(0xFFE8F5E9))),
            ColorPaletteData("Romantic Sunset Crimson", "Passionate crimson roses, honey amber, and peach wrapping.", listOf(Color(0xFF9E0B33), Color(0xFFFFB74D), Color(0xFFFFCC80))),
            ColorPaletteData("Sunshine Yellow & Peach", "Golden sunflower, bright yellow tulip, and fresh salmon blush.", listOf(Color(0xFFFFFB3F), Color(0xFFFFCC80), Color(0xFFFFAB91))),
            ColorPaletteData("Elegant White & Lavender", "Regal white lily and lavender violet twilight ribbons.", listOf(Color(0xFFF5F5F7), Color(0xFFB39DDB), Color(0xFFE1BEE7))),
            ColorPaletteData("Vibrant Wild Garden Mixed", "Rich mixed berries, deep violet, field gold, and wild herbs.", listOf(Color(0xFFD32F2F), Color(0xFFBA68C8), Color(0xFFFFD54F)))
        )
    }

    Column(
        modifier = Modifier
            .fillMaxSize()
            .verticalScroll(rememberScrollState())
            .padding(16.dp),
        verticalArrangement = Arrangement.spacedBy(16.dp)
    ) {
        Column {
            Text(
                text = "AI Custom Floral Designer",
                style = MaterialTheme.typography.titleLarge,
                fontWeight = FontWeight.ExtraBold,
                color = MaterialTheme.colorScheme.primary
            )
            Spacer(modifier = Modifier.height(4.dp))
            Text(
                text = "Configure custom flower selections, color palettes, and theme tags to compile an optimal engineering prompt for our master designer Gemini AI.",
                style = MaterialTheme.typography.bodyMedium,
                color = MaterialTheme.colorScheme.onBackground.copy(alpha = 0.7f)
            )
        }

        // 1. Occasion Tags Section
        Column {
            Text("Recipient Special Occasion:", fontWeight = FontWeight.Bold, fontSize = 14.sp)
            Spacer(modifier = Modifier.height(8.dp))
            Row(
                modifier = Modifier
                    .fillMaxWidth()
                    .horizontalScroll(rememberScrollState()),
                horizontalArrangement = Arrangement.spacedBy(8.dp)
            ) {
                viewModel.designerOccasions.forEach { occ ->
                    val isSelected = occ == occasion
                    FilterChip(
                        selected = isSelected,
                        onClick = { viewModel.setCreatorOccasion(occ) },
                        label = { Text(occ, fontWeight = FontWeight.Bold, fontSize = 13.sp) },
                        shape = RoundedCornerShape(16.dp),
                        colors = FilterChipDefaults.filterChipColors(
                            selectedContainerColor = MaterialTheme.colorScheme.primary,
                            selectedLabelColor = MaterialTheme.colorScheme.onPrimary,
                            containerColor = if (isDark) Color(0x1F291417) else Color(0x33FFFFFF),
                            labelColor = MaterialTheme.colorScheme.onBackground
                        ),
                        border = FilterChipDefaults.filterChipBorder(
                            enabled = true,
                            selected = isSelected,
                            borderColor = Color.White.copy(alpha = if (isDark) 0.15f else 0.5f),
                            selectedBorderColor = MaterialTheme.colorScheme.primary,
                            borderWidth = 1.dp,
                            selectedBorderWidth = 1.5.dp
                        ),
                        modifier = Modifier.testTag("tag_occasion_$occ")
                    )
                }
            }
        }

        // 2. Color Palette Selector Section
        Column {
            Text("Target Color Palette Theme:", fontWeight = FontWeight.Bold, fontSize = 14.sp)
            Spacer(modifier = Modifier.height(8.dp))
            Row(
                modifier = Modifier
                    .fillMaxWidth()
                    .horizontalScroll(rememberScrollState()),
                horizontalArrangement = Arrangement.spacedBy(10.dp)
            ) {
                palettes.forEach { pal ->
                    val isSelected = pal.name == colorTheme
                    Card(
                        onClick = { viewModel.setCreatorColor(pal.name) },
                        colors = CardDefaults.cardColors(
                            containerColor = if (isSelected) {
                                if (isDark) Color(0x2E6A1A31) else Color(0x1FE63B66)
                            } else {
                                if (isDark) Color(0x1F291417) else Color(0x99FFFFFF)
                            }
                        ),
                        shape = RoundedCornerShape(20.dp),
                        border = BorderStroke(
                            if (isSelected) 1.5.dp else 1.dp,
                            if (isSelected) MaterialTheme.colorScheme.primary else Color.White.copy(alpha = if (isDark) 0.1f else 0.5f)
                        ),
                        modifier = Modifier
                            .width(185.dp)
                            .testTag("palette_card_${pal.name}")
                    ) {
                        Column(modifier = Modifier.padding(12.dp)) {
                            Text(
                                text = pal.name,
                                fontWeight = FontWeight.ExtraBold,
                                fontSize = 12.sp,
                                maxLines = 1,
                                overflow = TextOverflow.Ellipsis
                            )
                            Spacer(modifier = Modifier.height(4.dp))
                            Text(
                                text = pal.description,
                                fontSize = 11.sp,
                                lineHeight = 14.sp,
                                color = MaterialTheme.colorScheme.onBackground.copy(alpha = 0.7f),
                                minLines = 2,
                                maxLines = 2,
                                overflow = TextOverflow.Ellipsis
                            )
                            Spacer(modifier = Modifier.height(8.dp))
                            Row(horizontalArrangement = Arrangement.spacedBy(6.dp)) {
                                pal.colors.forEach { color ->
                                    Box(
                                        modifier = Modifier
                                            .size(16.dp)
                                            .background(color, CircleShape)
                                            .border(1.dp, Color.White.copy(alpha = 0.5f), CircleShape)
                                    )
                                }
                            }
                        }
                    }
                }
            }
        }

        // 3. Flower Selection Panel Section
        Column {
            Row(
                modifier = Modifier.fillMaxWidth(),
                horizontalArrangement = Arrangement.SpaceBetween,
                verticalAlignment = Alignment.CenterVertically
            ) {
                Text("Incorporate Specific Flowers / Foliage:", fontWeight = FontWeight.Bold, fontSize = 14.sp)
                if (selectedStems.isNotEmpty()) {
                    TextButton(
                        onClick = { viewModel.clearStemSelections() },
                        contentPadding = PaddingValues(horizontal = 8.dp, vertical = 2.dp),
                        modifier = Modifier.height(30.dp)
                    ) {
                        Text("Reset All", fontSize = 11.sp, fontWeight = FontWeight.Bold, color = MaterialTheme.colorScheme.primary)
                    }
                }
            }
            Spacer(modifier = Modifier.height(6.dp))

            OutlinedCard(
                colors = CardDefaults.outlinedCardColors(
                    containerColor = if (isDark) Color(0x1F291417) else Color(0x66FFFFFF)
                ),
                border = BorderStroke(1.dp, Color.White.copy(alpha = if (isDark) 0.15f else 0.5f)),
                shape = RoundedCornerShape(24.dp),
                modifier = Modifier.fillMaxWidth()
            ) {
                Column(
                    modifier = Modifier.padding(12.dp),
                    verticalArrangement = Arrangement.spacedBy(8.dp)
                ) {
                    viewModel.designerStems.chunked(2).forEach { pair ->
                        Row(
                            modifier = Modifier.fillMaxWidth(),
                            horizontalArrangement = Arrangement.spacedBy(10.dp)
                        ) {
                            pair.forEach { stem ->
                                val currentCount = selectedStems[stem] ?: 0
                                val emoji = when {
                                    stem.contains("Crimson") -> "🌹"
                                    stem.contains("Pink") -> "🌸"
                                    stem.contains("Lily") -> "⚜️"
                                    stem.contains("Tulip") -> "🌷"
                                    stem.contains("Sunflower") -> "🌻"
                                    stem.contains("Lavender") -> "🪻"
                                    stem.contains("Daisy") -> "🌼"
                                    stem.contains("Eucalyptus") -> "🌿"
                                    else -> "🌺"
                                }
                                Row(
                                    modifier = Modifier
                                        .weight(1f)
                                        .background(
                                            if (currentCount > 0) {
                                                if (isDark) Color(0x1CE63B66) else Color(0x14E63B66)
                                            } else Color.Transparent,
                                            RoundedCornerShape(14.dp)
                                        )
                                        .border(
                                            1.dp,
                                            if (currentCount > 0) MaterialTheme.colorScheme.primary.copy(alpha = 0.5f) else Color.White.copy(alpha = if (isDark) 0.05f else 0.2f),
                                            RoundedCornerShape(14.dp)
                                        )
                                        .padding(vertical = 8.dp, horizontal = 10.dp),
                                    horizontalArrangement = Arrangement.SpaceBetween,
                                    verticalAlignment = Alignment.CenterVertically
                                ) {
                                    Row(
                                        modifier = Modifier.weight(1f),
                                        horizontalArrangement = Arrangement.spacedBy(6.dp),
                                        verticalAlignment = Alignment.CenterVertically
                                    ) {
                                        Text(text = emoji, fontSize = 16.sp, modifier = Modifier.testTag("flower_emoji_$stem"))
                                        Text(
                                            text = stem,
                                            fontSize = 11.sp,
                                            fontWeight = if (currentCount > 0) FontWeight.Bold else FontWeight.Medium,
                                            maxLines = 1,
                                            overflow = TextOverflow.Ellipsis,
                                            color = if (currentCount > 0) MaterialTheme.colorScheme.primary else MaterialTheme.colorScheme.onBackground
                                        )
                                    }
                                    Row(
                                        verticalAlignment = Alignment.CenterVertically,
                                        horizontalArrangement = Arrangement.spacedBy(4.dp)
                                    ) {
                                        IconButton(
                                            onClick = { viewModel.updateStemCount(stem, currentCount - 1) },
                                            modifier = Modifier
                                                .size(22.dp)
                                                .background(
                                                    if (isDark) Color(0x1F291417) else Color(0x19FFFFFF),
                                                    CircleShape
                                                )
                                                .testTag("stem_decrement_$stem")
                                        ) {
                                            Icon(
                                                imageVector = Icons.Default.Remove,
                                                contentDescription = "Remove",
                                                tint = MaterialTheme.colorScheme.onBackground,
                                                modifier = Modifier.size(10.dp)
                                            )
                                        }
                                        Text(
                                            text = currentCount.toString(),
                                            fontSize = 12.sp,
                                            fontWeight = FontWeight.ExtraBold,
                                            modifier = Modifier.widthIn(min = 12.dp).testTag("stem_count_$stem"),
                                            textAlign = TextAlign.Center
                                        )
                                        IconButton(
                                            onClick = { viewModel.updateStemCount(stem, currentCount + 1) },
                                            modifier = Modifier
                                                .size(22.dp)
                                                .background(
                                                    if (isDark) Color(0x1F291417) else Color(0x19FFFFFF),
                                                    CircleShape
                                                )
                                                .testTag("stem_increment_$stem")
                                        ) {
                                            Icon(
                                                imageVector = Icons.Default.Add,
                                                contentDescription = "Add",
                                                tint = MaterialTheme.colorScheme.onBackground,
                                                modifier = Modifier.size(10.dp)
                                            )
                                        }
                                    }
                                }
                            }
                        }
                    }
                }
            }
        }

        // 4. Dynamic Compiled Prompt Engineering Panel
        Column {
            Row(
                modifier = Modifier.fillMaxWidth(),
                horizontalArrangement = Arrangement.SpaceBetween,
                verticalAlignment = Alignment.CenterVertically
            ) {
                Text("AI Design Prompt Blueprint:", fontWeight = FontWeight.Bold, fontSize = 14.sp)
                Text(
                    text = "Compiles in Real-Time",
                    fontSize = 10.sp,
                    color = MaterialTheme.colorScheme.primary,
                    fontWeight = FontWeight.ExtraBold
                )
            }
            Spacer(modifier = Modifier.height(8.dp))
            OutlinedTextField(
                value = prompt,
                onValueChange = { viewModel.setCreatorPrompt(it) },
                colors = OutlinedTextFieldDefaults.colors(
                    focusedContainerColor = if (isDark) Color(0x25FFFFFF) else Color(0x80FFFFFF),
                    unfocusedContainerColor = if (isDark) Color(0x10FFFFFF) else Color(0x40FFFFFF),
                    focusedBorderColor = MaterialTheme.colorScheme.primary,
                    unfocusedBorderColor = Color.White.copy(alpha = if (isDark) 0.15f else 0.5f)
                ),
                textStyle = MaterialTheme.typography.bodyMedium.copy(
                    fontFamily = FontFamily.Monospace,
                    fontSize = 12.sp,
                    lineHeight = 16.sp
                ),
                modifier = Modifier
                    .fillMaxWidth()
                    .height(130.dp)
                    .testTag("creator_prompt_input"),
                shape = RoundedCornerShape(20.dp)
            )
        }

        // 5. Generate custom design button
        Button(
            onClick = { viewModel.generateCustomBouquet() },
            enabled = !isGenerating,
            colors = ButtonDefaults.buttonColors(
                containerColor = MaterialTheme.colorScheme.primary
            ),
            modifier = Modifier
                .fillMaxWidth()
                .height(52.dp)
                .testTag("generate_bouquet_button")
        ) {
            if (isGenerating) {
                CircularProgressIndicator(
                    color = MaterialTheme.colorScheme.onPrimary,
                    modifier = Modifier.size(24.dp)
                )
                Spacer(modifier = Modifier.width(10.dp))
                Text("AI Bouquet Designer Arranging...")
            } else {
                Icon(Icons.Default.AutoAwesome, contentDescription = null)
                Spacer(modifier = Modifier.width(8.dp))
                Text("Generate Custom Bouquet Design", fontWeight = FontWeight.Bold)
            }
        }

        // Display results
        AnimatedVisibility(
            visible = generatedBouquet != null || generationError != null,
            enter = expandVertically() + fadeIn(),
            exit = shrinkVertically() + fadeOut()
        ) {
            Column(verticalArrangement = Arrangement.spacedBy(16.dp)) {
                if (generationError != null) {
                    Card(
                        colors = CardDefaults.cardColors(containerColor = MaterialTheme.colorScheme.errorContainer),
                        modifier = Modifier.fillMaxWidth()
                    ) {
                        Text(
                            text = generationError ?: "Error occurred",
                            color = MaterialTheme.colorScheme.onErrorContainer,
                            modifier = Modifier.padding(16.dp),
                            fontWeight = FontWeight.Medium
                        )
                    }
                }

                generatedBouquet?.let { bouquet ->
                    val isDark = isSystemInDarkTheme()
                    Column(
                        modifier = Modifier
                            .fillMaxWidth()
                            .background(
                                if (isDark) Color(0x33FFAEB9) else Color(0x66FFFFFF),
                                RoundedCornerShape(24.dp)
                            )
                            .border(1.dp, Color.White.copy(alpha = if (isDark) 0.15f else 0.5f), RoundedCornerShape(24.dp))
                            .padding(16.dp),
                        verticalArrangement = Arrangement.spacedBy(12.dp)
                    ) {
                        Row(
                            horizontalArrangement = Arrangement.SpaceBetween,
                            verticalAlignment = Alignment.CenterVertically,
                            modifier = Modifier.fillMaxWidth()
                        ) {
                            Text(
                                text = "Design Approved! ✨",
                                style = MaterialTheme.typography.titleMedium,
                                fontWeight = FontWeight.ExtraBold,
                                color = MaterialTheme.colorScheme.primary
                            )
                            IconButton(onClick = { viewModel.clearGeneratedBouquet() }) {
                                Icon(Icons.Default.Close, contentDescription = "Clear")
                            }
                        }

                        Row(
                            modifier = Modifier.fillMaxWidth(),
                            horizontalArrangement = Arrangement.spacedBy(16.dp)
                        ) {
                            // Render custom bouquet visually
                            BouquetVisual(
                                stems = bouquet.stems,
                                wrapping = bouquet.wrapping,
                                ribbon = bouquet.ribbon,
                                modifier = Modifier
                                    .size(110.dp)
                                    .shadow(2.dp, CircleShape)
                            )

                            Column(modifier = Modifier.weight(1f)) {
                                Text(
                                    text = bouquet.name,
                                    style = MaterialTheme.typography.titleLarge,
                                    fontWeight = FontWeight.Bold,
                                    color = MaterialTheme.colorScheme.onBackground
                                )
                                Spacer(modifier = Modifier.height(4.dp))
                                Text(
                                    text = bouquet.description,
                                    style = MaterialTheme.typography.bodyMedium,
                                    color = MaterialTheme.colorScheme.onBackground.copy(alpha = 0.8f)
                                )
                                Spacer(modifier = Modifier.height(8.dp))
                                Text(
                                    text = "$${String.format("%.2f", bouquet.price)}",
                                    style = MaterialTheme.typography.titleMedium,
                                    color = MaterialTheme.colorScheme.primary,
                                    fontWeight = FontWeight.ExtraBold
                                )
                            }
                        }

                        Divider(color = MaterialTheme.colorScheme.onBackground.copy(alpha = 0.08f))

                        // Custom stem details
                        Column(verticalArrangement = Arrangement.spacedBy(4.dp)) {
                            Text(
                                text = "STEM ALLOCATIONS",
                                fontSize = 11.sp,
                                fontWeight = FontWeight.ExtraBold,
                                color = MaterialTheme.colorScheme.primary,
                                letterSpacing = 1.sp
                            )
                            bouquet.stems.entries.forEach { (stemName, quant) ->
                                Row(
                                    modifier = Modifier.fillMaxWidth(),
                                    horizontalArrangement = Arrangement.SpaceBetween
                                ) {
                                    Text(stemName, style = MaterialTheme.typography.bodyMedium)
                                    Text("${quant}x stems", fontWeight = FontWeight.Bold, style = MaterialTheme.typography.bodyMedium)
                                }
                            }
                        }

                        // Wrapper/Ribbon details
                        Row(
                            modifier = Modifier.fillMaxWidth(),
                            horizontalArrangement = Arrangement.spacedBy(16.dp)
                        ) {
                            Column(modifier = Modifier.weight(1f)) {
                                Text(
                                    text = "DESIGN WRAPPING",
                                    fontSize = 11.sp,
                                    fontWeight = FontWeight.ExtraBold,
                                    color = MaterialTheme.colorScheme.primary,
                                    letterSpacing = 1.sp
                                )
                                Text(bouquet.wrapping, style = MaterialTheme.typography.bodyMedium, maxLines = 1, overflow = TextOverflow.Ellipsis)
                            }
                            Column(modifier = Modifier.weight(1f)) {
                                Text(
                                    text = "DESIGN RIBBON",
                                    fontSize = 11.sp,
                                    fontWeight = FontWeight.ExtraBold,
                                    color = MaterialTheme.colorScheme.primary,
                                    letterSpacing = 1.sp
                                )
                                Text(bouquet.ribbon, style = MaterialTheme.typography.bodyMedium, maxLines = 1, overflow = TextOverflow.Ellipsis)
                            }
                        }

                        Spacer(modifier = Modifier.height(4.dp))

                        Button(
                            onClick = {
                                viewModel.addBouquetToCart(bouquet)
                                viewModel.clearGeneratedBouquet()
                                viewModel.selectTab(2) // go to cart
                            },
                            colors = ButtonDefaults.buttonColors(
                                containerColor = MaterialTheme.colorScheme.primary
                            ),
                            modifier = Modifier
                                .fillMaxWidth()
                                .height(48.dp)
                                .testTag("add_custom_bouquet_to_cart_button")
                        ) {
                            Icon(Icons.Default.ShoppingCart, contentDescription = null)
                            Spacer(modifier = Modifier.width(8.dp))
                            Text("Add Custom Bouquet to Cart ($${String.format("%.2f", bouquet.price)})", fontWeight = FontWeight.Bold)
                        }
                    }
                }
            }
        }
    }
}

@Composable
fun CartTab(viewModel: BloomViewModel) {
    val items by viewModel.cartItems.collectAsStateWithLifecycle()
    val rName by viewModel.recipientName.collectAsStateWithLifecycle()
    val rAddress by viewModel.recipientAddress.collectAsStateWithLifecycle()
    val rPhone by viewModel.recipientPhone.collectAsStateWithLifecycle()
    val rNote by viewModel.deliveryNote.collectAsStateWithLifecycle()
    val sameDay by viewModel.sameDayShipping.collectAsStateWithLifecycle()

    val subtotal = remember(items) { items.sumOf { it.price * it.quantity } }
    val shippingFee = if (sameDay) 9.99 else 4.99
    val total = subtotal + shippingFee

    if (items.isEmpty()) {
        Box(
            modifier = Modifier.fillMaxSize(),
            contentAlignment = Alignment.Center
        ) {
            Column(
                horizontalAlignment = Alignment.CenterHorizontally,
                verticalArrangement = Arrangement.spacedBy(16.dp),
                modifier = Modifier.padding(32.dp)
            ) {
                Icon(
                    imageVector = Icons.Default.ShoppingBag,
                    contentDescription = null,
                    tint = MaterialTheme.colorScheme.primary.copy(alpha = 0.3f),
                    modifier = Modifier.size(90.dp)
                )
                Text(
                    text = "Your bouquet cart is empty",
                    style = MaterialTheme.typography.titleLarge,
                    fontWeight = FontWeight.Bold,
                    color = MaterialTheme.colorScheme.onBackground.copy(alpha = 0.6f)
                )
                Text(
                    text = "A flower does not think of competing with the flower next to it. It just blooms. Go find your perfect florist gift!",
                    textAlign = TextAlign.Center,
                    style = MaterialTheme.typography.bodyMedium,
                    color = MaterialTheme.colorScheme.onBackground.copy(alpha = 0.5f)
                )
                Button(
                    onClick = { viewModel.selectTab(0) }
                ) {
                    Text("Explore Fresh Bouquets")
                }
            }
        }
        return
    }

    LazyColumn(
        modifier = Modifier
            .fillMaxSize()
            .testTag("checkout_page_scroll"),
        contentPadding = PaddingValues(16.dp),
        verticalArrangement = Arrangement.spacedBy(16.dp)
    ) {
        item {
            Text(
                text = "Shopping Basket & Checkout",
                style = MaterialTheme.typography.titleLarge,
                fontWeight = FontWeight.Bold,
                color = MaterialTheme.colorScheme.primary
            )
        }

        items(items) { item ->
            CartItemCard(
                item = item,
                onDelete = { viewModel.removeCartItem(item.id) }
            )
        }

        // Delivery form
        item {
            val isDark = isSystemInDarkTheme()
            Card(
                colors = CardDefaults.cardColors(
                    containerColor = if (isDark) Color(0x1F291417) else Color(0x99FFFFFF)
                ),
                shape = RoundedCornerShape(24.dp),
                border = BorderStroke(1.dp, Color.White.copy(alpha = if (isDark) 0.15f else 0.5f)),
                modifier = Modifier.fillMaxWidth().shadow(1.dp, RoundedCornerShape(24.dp))
            ) {
                Column(
                    modifier = Modifier.padding(16.dp),
                    verticalArrangement = Arrangement.spacedBy(12.dp)
                ) {
                    Text(
                        text = "Same-Day Hand-tied Shipping Address",
                        style = MaterialTheme.typography.titleMedium,
                        fontWeight = FontWeight.Bold,
                        color = MaterialTheme.colorScheme.primary
                    )

                    OutlinedTextField(
                        value = rName,
                        onValueChange = { viewModel.setRecipientName(it) },
                        label = { Text("Recipient Name") },
                        colors = OutlinedTextFieldDefaults.colors(
                            focusedContainerColor = if (isDark) Color(0x25FFFFFF) else Color(0x80FFFFFF),
                            unfocusedContainerColor = if (isDark) Color(0x10FFFFFF) else Color(0x40FFFFFF),
                            focusedBorderColor = MaterialTheme.colorScheme.primary,
                            unfocusedBorderColor = Color.White.copy(alpha = if (isDark) 0.15f else 0.5f)
                        ),
                        modifier = Modifier.fillMaxWidth().testTag("recipient_name_input"),
                        shape = RoundedCornerShape(20.dp),
                        singleLine = true
                    )

                    OutlinedTextField(
                        value = rAddress,
                        onValueChange = { viewModel.setRecipientAddress(it) },
                        label = { Text("Full Delivery Street Address") },
                        colors = OutlinedTextFieldDefaults.colors(
                            focusedContainerColor = if (isDark) Color(0x25FFFFFF) else Color(0x80FFFFFF),
                            unfocusedContainerColor = if (isDark) Color(0x10FFFFFF) else Color(0x40FFFFFF),
                            focusedBorderColor = MaterialTheme.colorScheme.primary,
                            unfocusedBorderColor = Color.White.copy(alpha = if (isDark) 0.15f else 0.5f)
                        ),
                        modifier = Modifier.fillMaxWidth().testTag("recipient_address_input"),
                        shape = RoundedCornerShape(20.dp)
                    )

                    OutlinedTextField(
                        value = rPhone,
                        onValueChange = { viewModel.setRecipientPhone(it) },
                        label = { Text("Recipient Contact Phone") },
                        colors = OutlinedTextFieldDefaults.colors(
                            focusedContainerColor = if (isDark) Color(0x25FFFFFF) else Color(0x80FFFFFF),
                            unfocusedContainerColor = if (isDark) Color(0x10FFFFFF) else Color(0x40FFFFFF),
                            focusedBorderColor = MaterialTheme.colorScheme.primary,
                            unfocusedBorderColor = Color.White.copy(alpha = if (isDark) 0.15f else 0.5f)
                        ),
                        modifier = Modifier.fillMaxWidth().testTag("recipient_phone_input"),
                        shape = RoundedCornerShape(20.dp),
                        keyboardOptions = KeyboardOptions(keyboardType = KeyboardType.Phone),
                        singleLine = true
                    )

                    OutlinedTextField(
                        value = rNote,
                        onValueChange = { viewModel.setDeliveryNote(it) },
                        label = { Text("Card Gift Message / Delivery Notes") },
                        placeholder = { Text("E.g. Happy Birthday Mom! Love always.") },
                        colors = OutlinedTextFieldDefaults.colors(
                            focusedContainerColor = if (isDark) Color(0x25FFFFFF) else Color(0x80FFFFFF),
                            unfocusedContainerColor = if (isDark) Color(0x10FFFFFF) else Color(0x40FFFFFF),
                            focusedBorderColor = MaterialTheme.colorScheme.primary,
                            unfocusedBorderColor = Color.White.copy(alpha = if (isDark) 0.15f else 0.5f)
                        ),
                        modifier = Modifier.fillMaxWidth().testTag("special_notes_input"),
                        shape = RoundedCornerShape(20.dp)
                    )

                    Row(
                        modifier = Modifier.fillMaxWidth(),
                        verticalAlignment = Alignment.CenterVertically
                    ) {
                        Column(modifier = Modifier.weight(1f)) {
                            Text(
                                "Same-Day Shipping",
                                fontWeight = FontWeight.Bold,
                                fontSize = 14.sp
                            )
                            Text(
                                "Express courier dispatch. Delivered within hours.",
                                fontSize = 11.sp,
                                color = MaterialTheme.colorScheme.onSurface.copy(alpha = 0.6f)
                            )
                        }
                        Switch(
                            checked = sameDay,
                            onCheckedChange = { viewModel.setSameDayShipping(it) },
                            modifier = Modifier.testTag("sameday_shipping_toggle")
                        )
                    }
                }
            }
        }

        // Bill summary
        item {
            val isDark = isSystemInDarkTheme()
            Card(
                colors = CardDefaults.cardColors(
                    containerColor = if (isDark) Color(0x35FFB4C1) else Color(0x22D63B66),
                    contentColor = if (isDark) Color(0xFFFFD5D9) else Color(0xFF8C1335)
                ),
                shape = RoundedCornerShape(24.dp),
                border = BorderStroke(1.dp, Color.White.copy(alpha = if (isDark) 0.15f else 0.5f)),
                modifier = Modifier.fillMaxWidth().shadow(1.dp, RoundedCornerShape(24.dp))
            ) {
                Column(
                    modifier = Modifier.padding(16.dp),
                    verticalArrangement = Arrangement.spacedBy(8.dp)
                ) {
                    Text(
                        text = "Bill Breakdown",
                        fontWeight = FontWeight.Bold,
                        style = MaterialTheme.typography.titleMedium
                    )

                    Row(
                        modifier = Modifier.fillMaxWidth(),
                        horizontalArrangement = Arrangement.SpaceBetween
                    ) {
                        Text("Flower subtotal", color = (if (isDark) Color(0xFFFFD5D9) else Color(0xFF8C1335)).copy(alpha = 0.8f))
                        Text("$${String.format("%.2f", subtotal)}", fontWeight = FontWeight.SemiBold)
                    }

                    Row(
                        modifier = Modifier.fillMaxWidth(),
                        horizontalArrangement = Arrangement.SpaceBetween
                    ) {
                        Text("Courier Shipping fee", color = (if (isDark) Color(0xFFFFD5D9) else Color(0xFF8C1335)).copy(alpha = 0.8f))
                        Text("$${String.format("%.2f", shippingFee)}", fontWeight = FontWeight.SemiBold)
                    }

                    HorizontalDivider(
                        color = (if (isDark) Color(0xFFFFD5D9) else Color(0xFF8C1335)).copy(alpha = 0.15f),
                        modifier = Modifier.padding(vertical = 4.dp)
                    )

                    Row(
                        modifier = Modifier.fillMaxWidth(),
                        horizontalArrangement = Arrangement.SpaceBetween,
                        verticalAlignment = Alignment.CenterVertically
                    ) {
                        Text("Total Bill Due", fontWeight = FontWeight.ExtraBold, fontSize = 16.sp)
                        Text("$${String.format("%.2f", total)}", fontWeight = FontWeight.ExtraBold, fontSize = 20.sp, color = MaterialTheme.colorScheme.primary)
                    }
                }
            }
        }

        // Place order button
        item {
            val isFormValid = rName.isNotBlank() && rAddress.isNotBlank() && rPhone.isNotBlank()
            Button(
                onClick = { viewModel.checkout() },
                enabled = isFormValid,
                modifier = Modifier
                    .fillMaxWidth()
                    .height(54.dp)
                    .testTag("place_order_button"),
                colors = ButtonDefaults.buttonColors(
                    containerColor = MaterialTheme.colorScheme.primary,
                    disabledContainerColor = MaterialTheme.colorScheme.primary.copy(alpha = 0.3f)
                )
            ) {
                Icon(Icons.Default.CheckCircle, contentDescription = null)
                Spacer(modifier = Modifier.width(8.dp))
                Text("Confirm Delivery & Send ($${String.format("%.2f", total)})", fontWeight = FontWeight.Bold, fontSize = 15.sp)
            }
            if (!isFormValid) {
                Text(
                    text = "* Please fill in Recipient Name, Delivery Address, and Contact Phone to complete order",
                    color = MaterialTheme.colorScheme.error,
                    fontSize = 11.sp,
                    textAlign = TextAlign.Center,
                    modifier = Modifier.fillMaxWidth().padding(top = 6.dp)
                )
            }
        }
    }
}

@Composable
fun OrdersTab(viewModel: BloomViewModel) {
    val ordersList by viewModel.orders.collectAsStateWithLifecycle()

    if (ordersList.isEmpty()) {
        Box(
            modifier = Modifier.fillMaxSize(),
            contentAlignment = Alignment.Center
        ) {
            Column(
                horizontalAlignment = Alignment.CenterHorizontally,
                verticalArrangement = Arrangement.spacedBy(16.dp),
                modifier = Modifier.padding(32.dp)
            ) {
                Icon(
                    imageVector = Icons.Default.LocalShipping,
                    contentDescription = null,
                    tint = MaterialTheme.colorScheme.primary.copy(alpha = 0.3f),
                    modifier = Modifier.size(90.dp)
                )
                Text(
                    text = "No active deliveries",
                    style = MaterialTheme.typography.titleLarge,
                    fontWeight = FontWeight.Bold,
                    color = MaterialTheme.colorScheme.onBackground.copy(alpha = 0.6f)
                )
                Text(
                    text = "Design a custom bouquet or pick a signature arrangement and submit you order. Same-day express tracking outputs here!",
                    textAlign = TextAlign.Center,
                    style = MaterialTheme.typography.bodyMedium,
                    color = MaterialTheme.colorScheme.onBackground.copy(alpha = 0.5f)
                )
                Button(
                    onClick = { viewModel.selectTab(0) }
                ) {
                    Text("Browse Shop Inventory")
                }
            }
        }
        return
    }

    LazyColumn(
        modifier = Modifier
            .fillMaxSize()
            .testTag("orders_list_page"),
        contentPadding = PaddingValues(16.dp),
        verticalArrangement = Arrangement.spacedBy(16.dp)
    ) {
        item {
            Row(
                modifier = Modifier.fillMaxWidth(),
                horizontalArrangement = Arrangement.SpaceBetween,
                verticalAlignment = Alignment.CenterVertically
            ) {
                Text(
                    text = "Active Bouquet Tracker",
                    style = MaterialTheme.typography.titleLarge,
                    fontWeight = FontWeight.Bold,
                    color = MaterialTheme.colorScheme.primary
                )
                TextButton(onClick = { viewModel.clearAllOrders() }) {
                    Text("Clear Logs", color = MaterialTheme.colorScheme.error)
                }
            }
        }

        items(ordersList, key = { it.id }) { order ->
            OrderCard(order = order)
        }
    }
}

// ======================== CARD WIDGETS ========================

@Composable
fun BouquetCard(
    bouquet: Bouquet,
    onAddClick: () -> Unit
) {
    val isDark = isSystemInDarkTheme()
    Card(
        colors = CardDefaults.cardColors(
            containerColor = if (isDark) Color(0x1F291417) else Color(0x99FFFFFF)
        ),
        shape = RoundedCornerShape(24.dp),
        border = BorderStroke(1.dp, Color.White.copy(alpha = if (isDark) 0.15f else 0.5f)),
        modifier = Modifier
            .fillMaxWidth()
            .shadow(1.dp, RoundedCornerShape(24.dp))
            .testTag("bouquet_card_${bouquet.id}")
    ) {
        Row(
            modifier = Modifier
                .fillMaxWidth()
                .padding(16.dp),
            horizontalArrangement = Arrangement.spacedBy(16.dp)
        ) {
            // Graphic Custom Vector flower visualizer! No blank space or missing URLs
            BouquetVisual(
                stems = bouquet.stems,
                wrapping = bouquet.wrapping,
                ribbon = bouquet.ribbon,
                modifier = Modifier
                    .size(115.dp)
                    .shadow(1.dp, CircleShape)
            )

            Column(modifier = Modifier.weight(1f)) {
                Row(
                    modifier = Modifier.fillMaxWidth(),
                    horizontalArrangement = Arrangement.SpaceBetween,
                    verticalAlignment = Alignment.CenterVertically
                ) {
                    Box(
                        modifier = Modifier
                            .background(
                                MaterialTheme.colorScheme.primaryContainer,
                                RoundedCornerShape(6.dp)
                            )
                            .padding(horizontal = 6.dp, vertical = 2.dp)
                    ) {
                        Text(
                            text = bouquet.occasion.uppercase(),
                            fontSize = 11.sp,
                            fontWeight = FontWeight.ExtraBold,
                            color = MaterialTheme.colorScheme.primary
                        )
                    }
                    Text(
                        text = "$${String.format("%.2f", bouquet.price)}",
                        style = MaterialTheme.typography.titleLarge,
                        fontWeight = FontWeight.ExtraBold,
                        color = MaterialTheme.colorScheme.primary
                    )
                }

                Spacer(modifier = Modifier.height(4.dp))

                Text(
                    text = bouquet.name,
                    style = MaterialTheme.typography.titleMedium,
                    fontWeight = FontWeight.Bold,
                    color = MaterialTheme.colorScheme.onBackground,
                    maxLines = 1,
                    overflow = TextOverflow.Ellipsis
                )

                Text(
                    text = bouquet.description,
                    style = MaterialTheme.typography.bodySmall,
                    color = MaterialTheme.colorScheme.onBackground.copy(alpha = 0.7f),
                    maxLines = 2,
                    overflow = TextOverflow.Ellipsis,
                    lineHeight = 16.sp
                )

                Spacer(modifier = Modifier.height(8.dp))

                HorizontalDivider(color = Color.White.copy(alpha = if (isDark) 0.15f else 0.5f))

                Spacer(modifier = Modifier.height(8.dp))

                Row(
                    modifier = Modifier.fillMaxWidth(),
                    horizontalArrangement = Arrangement.SpaceBetween,
                    verticalAlignment = Alignment.CenterVertically
                ) {
                    Text(
                        text = "Includes: ${bouquet.getStemsString().take(28)}...",
                        maxLines = 1,
                        fontSize = 10.sp,
                        fontWeight = FontWeight.SemiBold,
                        color = MaterialTheme.colorScheme.onBackground.copy(alpha = 0.6f),
                        overflow = TextOverflow.Ellipsis,
                        modifier = Modifier.weight(1f)
                    )

                    Button(
                        onClick = onAddClick,
                        colors = ButtonDefaults.buttonColors(
                            containerColor = MaterialTheme.colorScheme.primary
                        ),
                        contentPadding = PaddingValues(horizontal = 12.dp, vertical = 6.dp),
                        shape = RoundedCornerShape(12.dp),
                        modifier = Modifier
                            .height(34.dp)
                            .testTag("add_to_cart_button_${bouquet.id}")
                    ) {
                        Icon(Icons.Default.Add, contentDescription = null, modifier = Modifier.size(16.dp))
                        Spacer(modifier = Modifier.width(4.dp))
                        Text("Add", fontWeight = FontWeight.Bold, fontSize = 12.sp)
                    }
                }
            }
        }
    }
}

@Composable
fun CartItemCard(
    item: CartItem,
    onDelete: () -> Unit
) {
    val isDark = isSystemInDarkTheme()
    Card(
        colors = CardDefaults.cardColors(
            containerColor = if (isDark) Color(0x1F291417) else Color(0x99FFFFFF)
        ),
        shape = RoundedCornerShape(20.dp),
        border = BorderStroke(1.dp, Color.White.copy(alpha = if (isDark) 0.15f else 0.5f)),
        modifier = Modifier.fillMaxWidth().shadow(1.dp, RoundedCornerShape(20.dp))
    ) {
        Row(
            modifier = Modifier
                .fillMaxWidth()
                .padding(12.dp),
            horizontalArrangement = Arrangement.spacedBy(12.dp),
            verticalAlignment = Alignment.CenterVertically
        ) {
            // Render beautiful bouquet composition parsed
            val parsedBouquet = remember(item) { item.toBouquet() }
            BouquetVisual(
                stems = parsedBouquet.stems,
                wrapping = parsedBouquet.wrapping,
                ribbon = parsedBouquet.ribbon,
                modifier = Modifier
                    .size(65.dp)
                    .clip(CircleShape)
            )

            Column(modifier = Modifier.weight(1f)) {
                Text(
                    text = item.name,
                    fontWeight = FontWeight.Bold,
                    fontSize = 15.sp,
                    maxLines = 1,
                    overflow = TextOverflow.Ellipsis
                )
                Text(
                    text = "QTY: ${item.quantity}  •  $${String.format("%.2f", item.price * item.quantity)}",
                    fontWeight = FontWeight.Bold,
                    color = MaterialTheme.colorScheme.primary,
                    fontSize = 13.sp
                )
                if (item.note.isNotBlank()) {
                    Text(
                        text = "Message: \"${item.note}\"",
                        fontSize = 11.sp,
                        color = MaterialTheme.colorScheme.onSurface.copy(alpha = 0.6f),
                        maxLines = 1,
                        overflow = TextOverflow.Ellipsis
                    )
                }
            }

            IconButton(
                onClick = onDelete,
                modifier = Modifier.testTag("delete_cart_item_${item.bouquetId}")
            ) {
                Icon(
                    imageVector = Icons.Default.Delete,
                    contentDescription = "Delete",
                    tint = MaterialTheme.colorScheme.error.copy(alpha = 0.7f)
                )
            }
        }
    }
}

@Composable
fun OrderCard(order: Order) {
    val isDark = isSystemInDarkTheme()
    Card(
        colors = CardDefaults.cardColors(
            containerColor = if (isDark) Color(0x1F291417) else Color(0x99FFFFFF)
        ),
        shape = RoundedCornerShape(24.dp),
        border = BorderStroke(1.dp, Color.White.copy(alpha = if (isDark) 0.15f else 0.5f)),
        modifier = Modifier
            .fillMaxWidth()
            .shadow(1.dp, RoundedCornerShape(24.dp))
            .testTag("order_card_${order.id}")
    ) {
        Column(
            modifier = Modifier.padding(16.dp),
            verticalArrangement = Arrangement.spacedBy(10.dp)
        ) {
            Row(
                modifier = Modifier.fillMaxWidth(),
                horizontalArrangement = Arrangement.SpaceBetween,
                verticalAlignment = Alignment.CenterVertically
            ) {
                Column {
                    Text(
                        text = "Order #${100 + order.id}",
                        fontWeight = FontWeight.ExtraBold,
                        style = MaterialTheme.typography.titleMedium
                    )
                    Text(
                        text = "Placed at Same-day shipping",
                        fontSize = 11.sp,
                        color = MaterialTheme.colorScheme.onSurface.copy(alpha = 0.6f)
                    )
                }

                // Status pill color
                val (statusBg, statusFg) = when (order.status) {
                    "Placed" -> Pair(Color(0xFFE0E0E0), Color(0xFF616161))
                    "Arranging Bouquet" -> Pair(Color(0xFFFFE0B2), Color(0xFFE65100))
                    "With Dispatch Courier" -> Pair(Color(0xFFE1F5FE), Color(0xFF01579B))
                    "Out for same-day delivery" -> Pair(Color(0xFFE8EAF6), Color(0xFF1E88E5))
                    "Delivered" -> Pair(Color(0xFFE8F5E9), Color(0xFF2E7D32))
                    else -> Pair(Color(0xFFE0E0E0), Color(0xFF616161))
                }

                Box(
                    modifier = Modifier
                        .background(statusBg, RoundedCornerShape(30.dp))
                        .padding(horizontal = 10.dp, vertical = 4.dp)
                ) {
                    Text(
                        text = order.status.uppercase(),
                        fontWeight = FontWeight.Black,
                        fontSize = 11.sp,
                        color = statusFg
                    )
                }
            }

            HorizontalDivider(color = Color.White.copy(alpha = if (isDark) 0.15f else 0.5f))

            Row(verticalAlignment = Alignment.Top) {
                Icon(
                    imageVector = Icons.Default.LocalShipping,
                    contentDescription = null,
                    tint = MaterialTheme.colorScheme.primary,
                    modifier = Modifier
                        .size(24.dp)
                        .padding(top = 2.dp)
                )
                Spacer(modifier = Modifier.width(10.dp))
                Column {
                    Text(
                        text = "Recipient: ${order.recipientName}",
                        fontWeight = FontWeight.SemiBold,
                        fontSize = 13.sp
                    )
                    Text(
                        text = "Address: ${order.recipientAddress}",
                        fontSize = 12.sp,
                        color = MaterialTheme.colorScheme.onSurface.copy(alpha = 0.7f),
                        lineHeight = 16.sp
                    )
                    Text(
                        text = "Phone Contact: ${order.recipientPhone}",
                        fontSize = 12.sp,
                        color = MaterialTheme.colorScheme.onSurface.copy(alpha = 0.7f)
                    )
                    if (order.deliveryMessage.isNotBlank()) {
                        Text(
                            text = "Card Message: \"${order.deliveryMessage}\"",
                            fontSize = 12.sp,
                            fontWeight = FontWeight.Medium,
                            color = MaterialTheme.colorScheme.primary
                        )
                    }
                }
            }

            HorizontalDivider(color = Color.White.copy(alpha = if (isDark) 0.15f else 0.5f))

            Row(
                modifier = Modifier.fillMaxWidth(),
                horizontalArrangement = Arrangement.SpaceBetween,
                verticalAlignment = Alignment.CenterVertically
            ) {
                Column {
                    Text(
                        text = "BOUQUETS IN ORDER",
                        fontSize = 10.sp,
                        fontWeight = FontWeight.ExtraBold,
                        color = MaterialTheme.colorScheme.primary,
                        letterSpacing = 1.sp
                    )
                    Text(
                        text = order.itemsSummary,
                        fontSize = 13.sp,
                        fontWeight = FontWeight.Medium,
                        color = MaterialTheme.colorScheme.onBackground.copy(alpha = 0.8f)
                    )
                }
                Text(
                    text = "$${String.format("%.2f", order.totalCost)}",
                    fontWeight = FontWeight.ExtraBold,
                    fontSize = 18.sp,
                    color = MaterialTheme.colorScheme.primary
                )
            }
        }
    }
}
