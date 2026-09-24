# Material → miuix Migration Rules (v0.9.4)

Base package: `top.yukonga.miuix.kmp.*`
Modules already on classpath via `libs.bundles.miuix` (miuix-ui, miuix-preference, miuix-icons).

## Critical API differences (read before rewriting)

### Scaffold
```kotlin
// FROM Material
Scaffold(topBar = { TopAppBar(title = { Text("T") }, ...) }, floatingActionButton = { FAB }, snackbarHost = {...}) { padding -> }
// TO miuix
import top.yukonga.miuix.kmp.basic.Scaffold
import top.yukonga.miuix.kmp.basic.TopAppBar
Scaffold(
    topBar = { TopAppBar(title = "T", navigationIcon = { BackIcon }, actions = { ... }) },
    floatingActionButton = { ... },
) { padding -> }
```
- miuix Scaffold content still receives `PaddingValues`
- Default `popupHost = { MiuixPopupHost() }` enables OverlayDialog/OverlayBottomSheet
- `containerColor` defaults to `MiuixTheme.colorScheme.surface`
- Drop `ScaffoldDefaults.contentWindowInsets.exclude(...)` patterns unless critical — use defaults

### TopAppBar
```kotlin
// Material: title is @Composable, navigationIcon is composable
TopAppBar(title = { Text(...) }, navigationIcon = { IconButton { Icon(ArrowBack) } }, actions = { ... })
// miuix: title is String
import top.yukonga.miuix.kmp.basic.TopAppBar
TopAppBar(
    title = stringResource(RStrings.xxx),  // or literal / computed string
    navigationIcon = {
        IconButton(onClick = onBack) {
            Icon(MiuixIcons.Back, contentDescription = null)  // or keep Icons.AutoMirrored.Rounded.ArrowBack
        }
    },
    actions = { ... },  // RowScope
)
```
- For back button pattern used everywhere: create/use a small helper or inline IconButton with `MiuixIcons.Back`
- MediumTopAppBar / collapsing: use miuix `TopAppBar` with `scrollBehavior = MiuixScrollBehavior()` if needed, or SmallTopAppBar

### NavigationSuiteScaffold (MainScreen only)
Replace with bottom `NavigationBar` when compact; keep adaptive decision if possible:
```kotlin
import top.yukonga.miuix.kmp.basic.NavigationBar
import top.yukonga.miuix.kmp.basic.NavigationBarItem
NavigationBar {
    NavigationBarItem(
        selected = page == screen,
        onClick = { ... },
        icon = MiuixIcons.Home,  // ImageVector REQUIRED — not a slot!
        label = stringResource(title),
    )
}
```
- Note: `icon` is `ImageVector`, not `@Composable`. Badge on profile: miuix NavigationBarItem may support badge — check; if not, append to label or wrap icon manually.
- For adaptive rail: `NavigationRail` + `NavigationRailItem` same shape.

### TabRow
```kotlin
// Material PrimaryTabRow/PrimaryScrollableTabRow + Tab composables
// miuix:
TabRow(
    tabs = listOf("Illust", "Novel"),  // List<String>
    selectedTabIndex = selected,
    onTabSelected = { selected = it },
)
```
- You must extract tab titles as strings (use stringResource at call site into a List)
- Selection state must be Int index

### AlertDialog → OverlayDialog / WindowDialog
```kotlin
// Material
if (show) AlertDialog(onDismissRequest = {...}, title = {Text}, confirmButton = {...}, dismissButton = {...}, text = {...})
// miuix — prefer OverlayDialog inside Scaffold; WindowDialog if no Scaffold
if (show) OverlayDialog(  // or WindowDialog
    show = true,
    title = "Title",
    summary = "Optional body text",  // or put content in content{}
    onDismissRequest = { show = false },
    content = {
        Row(horizontalArrangement = Arrangement.SpaceBetween, modifier = Modifier.fillMaxWidth()) {
            TextButton(text = "Cancel", onClick = { show = false }, modifier = Modifier.weight(1f))
            Spacer(Modifier.width(16.dp))
            TextButton(
                text = "OK",
                onClick = { ...; show = false },
                modifier = Modifier.weight(1f),
                colors = ButtonDefaults.textButtonColorsPrimary(),
            )
        }
    },
)
```
- `confirmButton`/`dismissButton`/`text` slots don't exist — compose buttons in `content`
- Keep `show` state pattern (`var show by remember...`)

### ModalBottomSheet → OverlayBottomSheet
```kotlin
OverlayBottomSheet(
    show = sheetVisible,
    title = "Title",  // optional
    onDismissRequest = { sheetVisible = false },
    content = { /* list of options */ },
)
```
- Needs Scaffold ancestor (Miuix PopupHost) — screens already have Scaffold after migration

### ListItem → preferences / BasicComponent
```kotlin
// Switch row
SwitchPreference(
    checked = value,
    onCheckedChange = { ... },
    title = stringResource(RStrings.x),
    summary = stringResource(RStrings.y),  // optional
    startAction = { Icon(MiuixIcons.Settings, null) },  // optional leading icon
)
// Navigation/arrow row
ArrowPreference(
    title = stringResource(...),
    summary = ...,  // optional
    startAction = { Icon(...) },
    onClick = { navigate() },
)
// Dropdown selector row (replaces DropDownSelector + ListItem)
OverlayDropdownPreference(
    items = listOf("A", "B", "C"),
    selectedIndex = index,
    title = stringResource(...),
    onSelectedIndexChange = { index = it },
)
// Generic clickable row
BasicComponent(
    title = "...",
    summary = "...",
    startAction = { Icon(...) },
    endActions = { Icon(MiuixIcons.ChevronForward, null) },
    onClick = { ... },
)
```

### Buttons
```kotlin
// Material Button → miuix Button (content lambda)
Button(onClick = { ... }, modifier = ...) { Text("OK") }
// Primary color:
Button(onClick = {}, colors = ButtonDefaults.buttonColorsPrimary()) { ... }
// OutlinedButton → use Button with secondary/default colors or TextButton
// TextButton material (confirm/dismiss) → miuix TextButton(text=, onClick=) which takes String text
TextButton(text = "Cancel", onClick = { ... })
```
- miuix `TextButton` signature: `TextButton(text: String, onClick, ...)` — NOT a content-lambda like Material

### TextField / OutlinedTextField
```kotlin
TextField(
    value = text,
    onValueChange = { text = it },
    modifier = ...,
    label = "Label",  // optional String
    singleLine = true,
)
```
- No `OutlinedTextField` — use miuix `TextField` for both
- Drop `TextFieldDefaults.outlinedTextFieldColors()` — use `TextFieldDefaults.textFieldColors()` if needed

### Progress
```kotlin
// Material CircularWavyProgressIndicator / CircularProgressIndicator
CircularProgressIndicator(modifier = ...)  // miuix, indeterminate when progress=null
LinearProgressIndicator(progress = fractionOrNull, modifier = ...)
```
- `CircularWavyProgressIndicator` → `CircularProgressIndicator`
- `LinearWavyProgressIndicator` → `LinearProgressIndicator`

### PullToRefresh
```kotlin
// Material PullToRefreshBox { content }
PullToRefresh(
    isRefreshing = refreshing,
    onRefresh = { refreshing = true; vm.refresh() },
    modifier = ...,
) {
    // content (usually the scrollable column)
}
```
- Remove `PullToRefreshDefaults` / `rememberPullToRefreshState` Material imports if unused
- Contract: set isRefreshing true in onRefresh, false when done

### DropdownMenu
```kotlin
// Prefer WindowDropdownMenu (no Scaffold needed) or OverlayDropdownMenu
WindowListPopup(  // or OverlayListPopup
    show = expanded,
    onDismissRequest = { expanded = false },
    content = {
        // list of clickable items using BasicComponent or custom
    },
)
```
- For preference dropdowns already migrated to OverlayDropdownPreference, delete DropdownMenu entirely

### Icons
- Prefer `MiuixIcons.X` from `top.yukonga.miuix.kmp.icon.MiuixIcons` + `import top.yukonga.miuix.kmp.icon.extended.X` for extended
- Basic always available: ArrowRight, ArrowUpDown, Check, Close, Search, SearchCleanup, Sidebar
- Extended examples: Back, Add, Delete, Download, Edit, Filter, Home, Image, Info, Lock, More, Play, Refresh, Settings, Share, Favorites, FavoritesFill, Ok, Send, Show, Hide, Sort, Clear, Copy, Help
- If no MiuixIcons equivalent for a Material icon, KEEP `Icons.Rounded.X` for now (don't block migration) — record for Phase 5
- ArrowBack → `MiuixIcons.Back` or keep `Icons.AutoMirrored.Rounded.ArrowBack`
- ArrowForwardIos / chevron → `MiuixIcons.ChevronForward` or `MiuixIcons.ArrowRight`

### Switch / Checkbox / RadioButton
```kotlin
// Switch — onCheckedChange is nullable
Switch(checked = v, onCheckedChange = { if (it != null) v = it })
// Checkbox — ToggleableState API
Checkbox(state = ToggleableState(v), onClick = { v = !v })
// RadioButton — onClick nullable
RadioButton(selected = v, onClick = { v = true })
```
- In preference rows prefer SwitchPreference / CheckboxPreference / RadioButtonPreference

### Color / Typography tokens
```kotlin
MaterialTheme.colorScheme.primary          → MiuixTheme.colorScheme.primary
.onSurfaceVariant                         → .onSurfaceVariantSummary  (or onSurfaceVariantActions for action icons)
.surfaceVariant                           → .surfaceVariant
.background                                → .background
.surface                                   → .surface
.onSurface                                 → .onSurface
.outline / .outlineVariant                 → .outline / .dividerLine
.onPrimaryContainer / .primaryContainer    → same names exist
.error                                     → .error
surfaceContainer / High / Lowest           → surfaceContainer / surfaceContainerHigh / surfaceContainerHighest

MaterialTheme.typography.bodyLarge    → MiuixTheme.textStyles.main
.bodyMedium                     → .body1
.bodySmall                      → .body2
.titleLarge                     → .title2
.titleMedium                    → .subtitle   (or title3)
.titleSmall                     → .subtitle
.labelLarge / .labelMedium      → .footnote1
.labelSmall                     → .footnote2
.headlineSmall                  → .title3
```
- Prefer minimal token swaps that preserve hierarchy; don't redesign type scale

### Text / Icon / IconButton
```kotlin
// Text — miuix Text has style default textStyles.main
Text(text = "...", style = MiuixTheme.textStyles.body1, color = ...)
// Icon
Icon(imageVector = MiuixIcons.X, contentDescription = null, modifier = Modifier.size(24.dp))
// IconButton — simpler API, no IconButtonDefaults.shapes()
IconButton(onClick = { ... }) { Icon(...) }
```
- Remove `IconButtonDefaults.iconButtonColors()` / `.shapes()` unless using miuix equivalents
- Long/double click custom IconButtonExt: keep the extension if needed but move package OUT of `androidx.compose.material3` — or reimplement with miuix IconButton + pointerInput. Prefer: rewrite as `com.mrl.pixiv.common.compose.ui.MiuixIconButton` with long-press support, update call sites in same PR if time; else leave IconButtonExt temporarily but change its base to not require ExperimentalMaterial3ExpressiveApi if possible.

Actually for speed: KEEP `IconButtonExt.kt` as-is for this pass (it still compiles with material3 on classpath) — only migrate call sites that use `IconButtonDefaults.*` visual styling heavily. IconButton itself can stay Material for one pass if needed. **Focus on Scaffold/TopAppBar/preference/dialog/sheet/theme tokens first.**

### FilterChip / DatePicker / SwipeToDismiss / Tooltip
- KEEP on material3 for this pass (add explicit material3 import if removing wildcard)
- miuix has no chip/date picker equivalent in 0.9.4

### Badge
```kotlin
Badge / BadgedBox → top.yukonga.miuix.kmp.basic.Badge / BadgedBox
```

### FAB
```kotlin
FloatingActionButton(onClick = { ... }) { Icon(...) }  // miuix
```
- Drop `SmallFloatingActionButton` — use regular with smaller size or keep material

### Divider
```kotlin
HorizontalDivider(modifier = ..., color = MiuixTheme.colorScheme.dividerLine)  // miuix default is fine
```

## Import cleanup rules
- Remove `import androidx.compose.material3.X` only when X is fully replaced
- Leave material3 imports for: adaptive APIs, DatePicker*, FilterChip, SwipeToDismiss, experimental opt-ins still needed, LocalContentColor/LocalTextStyle if still used from foundation (prefer MiuixTheme)
- `ExperimentalMaterial3Api` opt-ins: remove only if no longer required
- `ripple()` from material — remove; miuix provides indication via MiuixTheme/LocalIndication

## Constraints
1. Do NOT change ViewModel, navigation routes, state classes, analytics
2. Do NOT remove shared-element transitions or Navigation3 wiring
3. Keep composable public signatures stable when possible (callers depend on them)
4. One file at a time, preserve formatting/style of surrounding code
5. No comments unless matching existing style
6. Prefer mechanical replacement over redesign — keep layout structure (Column/Row/LazyColumn) identical; only swap chrome components
7. For ListItem with complex leading/trailing columns (IntrinsicSize centering), replace with SwitchPreference/ArrowPreference which handle layout
8. `rememberThrottleClick` — keep using it for onClick wrappers where it was used

## Worked examples (settings switch row)

BEFORE:
```kotlin
ListItem(
    onClick = rememberThrottleClick { ... toggle ... },
    shapes = ListItemDefaults.shapes(shape = RectangleShape),
    content = { Text(stringResource(RStrings.x)) },
    supportingContent = { Text(stringResource(RStrings.y)) },
    modifier = Modifier.height(IntrinsicSize.Min),
    leadingContent = {
        Column(Modifier.fillMaxHeight(), verticalArrangement = Arrangement.Center) {
            Icon(Icons.Rounded.VisibilityOff, null)
        }
    },
    trailingContent = {
        Column(Modifier.fillMaxHeight(), verticalArrangement = Arrangement.Center) {
            Switch(checked = v, onCheckedChange = { ... })
        }
    },
)
```

AFTER:
```kotlin
SwitchPreference(
    checked = v,
    onCheckedChange = { ... },
    title = stringResource(RStrings.x),
    summary = stringResource(RStrings.y),
    startAction = { Icon(Icons.Rounded.VisibilityOff, contentDescription = null) },
)
```

## Worked example — Scaffold + TopAppBar screen

BEFORE:
```kotlin
Scaffold(
    modifier = modifier,
    topBar = {
        TopAppBar(
            title = { Text(stringResource(RStrings.settings)) },
            navigationIcon = {
                IconButton(onClick = onBack, shapes = IconButtonDefaults.shapes()) {
                    Icon(Icons.AutoMirrored.Rounded.ArrowBack, null)
                }
            },
            actions = { ... },
        )
    },
    contentWindowInsets = ScaffoldDefaults.contentWindowInsets.exclude(WindowInsets.navigationBars),
) { padding -> ... }
```

AFTER:
```kotlin
Scaffold(
    modifier = modifier,
    topBar = {
        TopAppBar(
            title = stringResource(RStrings.settings),
            navigationIcon = {
                IconButton(onClick = onBack) {
                    Icon(MiuixIcons.Back, contentDescription = null)
                }
            },
            actions = { ... },
        )
    },
) { padding -> ... }
```
