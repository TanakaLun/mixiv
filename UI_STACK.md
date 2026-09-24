# UI Stack Analysis (Material 3 + Navigation 3)

> 目的：为 miuix 迁移提供现状盘点。仅 Android-only 构建（已裁剪 desktop/iOS）。
> 统计基于仓库当前源码（约 88 个文件引用 `androidx.compose.material3.*`，82 个 material-icons import）。

## 1. 依赖与主题

| 项 | 版本 / 位置 |
|---|---|
| Compose BOM (androidx) | `2026.09.00` (`gradle/libs.versions.toml`) |
| material3 (androidx) | `1.5.0-alpha28` |
| material3 (JetBrains CMP) | `1.12.0-alpha03` |
| material3-adaptive (JB) | `1.3.0-rc01` |
| material-icons-extended | `1.7.3` |
| Navigation3 runtime/ui | `1.1.7` / JB ui `1.1.2` |
| adaptive-navigation3 | `1.3.0` |
| markdown renderer (m3) | `0.45.0` (`AboutScreen`) |
| sonner toast | `0.4.0` |
| composewebview | `1.0.3` (`feature/login`) |

主题入口：

- `composeApp/.../theme/Theme.kt` → `PiPixivTheme` 包一层 `MaterialExpressiveTheme`
- light = `expressiveLightColorScheme()`，dark = `darkColorScheme()`
- `MainActivity` 使用 `dynamicLightColorScheme` / `dynamicDarkColorScheme`（动态取色）
- 全局无自定义 `Typography` / `Shapes` 注入（依赖 M3 默认 + expressiveness API）

## 2. Material3 组件清单（按 import 次数）

### 2.1 高频（≥10 文件级用法）

| 组件 / API | 约 import 数 | 典型用途 |
|---|---:|---|
| `Text` | 69 | 全站文案 |
| `Icon` | 60 | 图标 |
| `IconButton` | 44 | 顶栏/行内操作 |
| `MaterialTheme` | 43 | `colorScheme` / `typography` / `ripple` |
| `Scaffold` | 38–40 | 页面骨架 |
| `TopAppBar` | 36 | 各屏顶栏（含 `MediumTopAppBar`×1） |
| `IconButtonDefaults` | 28 | icon button 样式 |
| `ListItem` + `ListItemDefaults` | 18 | 列表行（设置/搜索历史等） |
| `CircularWavyProgressIndicator` | 17 | Expressive 波浪进度 |
| `TextButton` | 16 | 次要按钮 |
| `HorizontalDivider` | 15 | 分割线 |
| `Button` | 14 | 主按钮 |
| `PullToRefreshBox` / `rememberPullToRefreshState` / `PullToRefreshDefaults` | 13/12/12 | 下拉刷新 |
| `Card` | 12 | 卡片 |
| `Tab` | 11 | 标签 |
| `ripple` / `ScaffoldDefaults` / `DropdownMenuItem` / `AlertDialog` | 10 | ripple、Scaffold、下拉菜单、确认框 |

### 2.2 中频（3–9）

`Switch`(9)、`rememberBottomSheetState`/`SheetValue`(8)、`PrimaryTabRow`(7)、`OutlinedTextField`(7)、`ModalBottomSheet`(7)、`TextField`(6)、`Surface`(6)、`LocalContentColor`(6)、`FloatingActionButton`(6)、`FilterChip`(6)、`DropdownMenu`(6)、`OutlinedButton`(5)、`CircularProgressIndicator`(5)、`TextFieldDefaults`/`CardDefaults`(4)、`Badge`(4)、`LinearWavyProgressIndicator`/`Checkbox`/`TopAppBarDefaults`/`PrimaryScrollableTabRow`/`ButtonDefaults`(3)。

### 2.3 低频 / 特殊（1–2）

`ModalBottomSheet` helpers、`DatePicker` / `DateRangePicker` + Dialog/Defaults、`Slider`、`RadioButton`、`SwipeToDismissBox`、`TooltipBox` + `PlainTooltip`、`SmallFloatingActionButton`、`BottomAppBarDefaults`、`BadgedBox`、`AssistChip`、`SecondaryTabRow`、`ProvideTextStyle`、`LocalTextStyle`、`experimental` color schemes。

### 2.4 Adaptive / Navigation Suite

- `NavigationSuiteScaffold` + `NavigationSuiteScaffoldDefaults` + `currentWindowAdaptiveInfoV2` — 仅 `feature/main/.../MainScreen.kt`
- `WindowAdaptiveInfo`、`separatingVerticalHingeBounds`、`NavigationSuiteType`
- 分栏详情 enter/exit 见 `AdaptiveScene.kt`（split-pane）

### 2.5 自定义 / 侵入式 Material 扩展

- **包占用**：`common/ui/src/commonMain/kotlin/androidx/compose/material3/IconButtonExt.kt` — 在 `androidx.compose.material3` 包下追加扩展（迁移时需改包或删除）
- 无自有 `Scaffold`/`TopAppBar` 子类；无自定义 `ColorScheme` 构建器（仅 expressive 默认）

### 2.6 Material Icons（82 import）

风格以 **rounded** 为主，少量 filled / outlined / automirrored。代表：

`Icons.Rounded.{Home, Search, Settings, Person, Favorite, Download, Share, Visibility, Bookmark, Translate, AutoAwesome, ...}`  
`Icons.AutoMirrored.Rounded.{ArrowBack, ArrowForward, Sort, Logout, ...}`  
`Icons.Filled.{Close, Delete, EditCalendar, ...}`、`Icons.Outlined.{Info, WatchLater}`

> 迁移时需逐条映射到 `MiuixIcons`（skill: `miuix`），缺失图标用近似或 drawable 替代。

## 3. 导航架构与默认转场动画

技术栈：**Navigation 3**（`NavDisplay` + `entry<>` graph），非 Navigation2。

关键文件：

| 文件 | 职责 |
|---|---|
| `composeApp/.../navigation/AdaptiveScene.kt` | 自适应 Scene + 默认 enter/exit |
| `composeApp/.../navigation/Navigation3MainGraph.kt` | 全局 entry 图 + per-entry `transitionSpec` + `SharedTransitionLayout` |
| `common/core/.../animation/AnimationDefaults.kt` | `DefaultAnimationDuration = 200`，`DefaultFloatAnimationSpec = tween(200)` |
| `feature/main/.../MainScreen.kt` | 底部/侧栏 `NavigationSuiteScaffold` + tab 切换动画 |

### 3.1 默认 Scene 转场（无 entry 覆盖时）

`AdaptiveScene.kt` fallback：

- **enter**: `fadeIn(tween(220))`
- **exit**: `fadeOut(tween(140))`
- predictive pop 可为 `EnterTransition.None togetherWith ExitTransition.None`（抑制动画路径）

### 3.2 分栏 / Detail 模式（同文件）

- enter: `fadeIn(tween(200)) + slideInHorizontally(tween(220)) { it / 16 }`
- exit: `fadeOut(tween(140))`
- `suppressAnimation` 时为 `None`

### 3.3 Entry 级覆盖（`Navigation3MainGraph.kt`）

| Destination | transitionSpec | predictivePop |
|---|---|---|
| `PictureDeeplink` | `scaleIn(0.9)+fadeIn()` → `scaleOut(1.1)+fadeOut()`（默认 spring） | scale 反向 1.1↔0.9 |
| `ImagePreview` | `fadeIn(tween(200)) togetherWith fadeOut(tween(200))` | 同左 |
| `Picture` | scale 0.9/1.1 + fade，spec=`DefaultFloatAnimationSpec`(tween 200) | scale 反向 + fade |

其余 entry（Main、Setting、Search、Collection…）走 **默认 fade 220/140**。

### 3.4 主界面 Tab 切换（`MainScreen.kt`）

```text
enter: fadeIn(tween(220, delayMillis = 90))
exit:  fadeOut(tween(90))
```

### 3.5 Shared Element

- 根：`Navigation3MainGraph` 内 `SharedTransitionLayout`，经 `LocalSharedTransitionScope` 下发
- 使用点：`IllustItem`（`sharedBounds` + `sharedElement`）、`PictureScreen`、`ImagePreview`、`ProfileScreen` 相关
- `placeholderSize = SharedTransitionScope.PlaceholderSize.AnimatedSize`

## 4. 页面 / 模块规模（迁移工作量参考）

| Feature | `*Screen*.kt` 数（约） |
|---|---:|
| setting | 15–19 |
| main | 11 |
| login | 5 |
| picture | 3 |
| novel | 3 |
| collection / comment | 2 each |
| artwork / follow / history / image-preview / report | 1 each |

- `TopAppBar` 出现于 **36** 个文件；`Scaffold(` **40** 文件；`MaterialTheme` **43** 文件。
- 设置类 UI 集中在 `feature/setting`（大量 `ListItem`+`Switch` 组合 → 优先 `miuix-preference`）。

## 5. 迁移映射提示（Material → miuix）

> 完整步骤见 `MIGRATION_TODO.md`；此处仅组件对照草案。

| Material3 | miuix（0.9.x）方向 |
|---|---|
| `Scaffold` + `TopAppBar` | Miuix `Scaffold` / `TopAppBar`（Overlay* 需 Scaffold 祖先） |
| `NavigationBar` / `NavigationRail` / `NavigationSuiteScaffold` | Miuix `NavigationBar` / `NavigationRail`（自适应策略需自研或保留 adaptive 壳） |
| `ListItem` + `Switch` | `SwitchPreference` / `ArrowPreference` 等 `miuix-preference` |
| `AlertDialog` | `OverlayDialog` |
| `ModalBottomSheet` | 底部 Overlay（skill 中 OverlayDialog/BottomSheet 体系） |
| `CircularProgressIndicator` / Wavy | Miuix progress（查 skill API） |
| `Text` / `Button` / `IconButton` | Miuix 基础组件 + `MiuixIcons` |
| `MaterialExpressiveTheme` | `MiuixTheme` 包裹（skill 强制） |
| Navigation3 转场 | **保留 Navigation3**；优先只换 chrome，不动 `AdaptiveScene`/`transitionSpec`；或评估 `miuix-nav`（更名自 `miuix-navigation3-ui`，自研 runtime、零依赖 androidx.navigation3，接入=整套替换） |
| 毛玻璃 / textureBlur | `miuix-blur`（**minSdk 33**，应用 minSdk 26 → 需门控） |

### 5.1 Material Icons → MiuixIcons mapping

> 导出时间：Phase 5。权威图标列表：`docs/guide/icons.md`（miuix 源码）。
> 用法：`MiuixIcons.Name`（Regular 默认）+ `import top.yukonga.miuix.kmp.icon.extended.Name`；basic 用 `MiuixIcons.Basic.Name` + `import top.yukonga.miuix.kmp.icon.basic.Name`。

| Material | MiuixIcons | 状态 |
|---|---|---|
| `AutoMirrored.Rounded.ArrowBack` | `Back` | 可迁 |
| `AutoMirrored.Rounded.ArrowBackIos` | `ChevronBackward` | 可迁 |
| `AutoMirrored.Rounded.ArrowForward` | `Forward` | 可迁 |
| `AutoMirrored.Rounded.ArrowForwardIos` | `ChevronForward` | 可迁 |
| `AutoMirrored.Rounded.Sort` | `Sort` | 可迁 |
| `AutoMirrored.Rounded.ViewList` | `ListView` | 可迁 |
| `AutoMirrored.Rounded.Logout` | （无） | 暂留 / issue |
| `AutoMirrored.Filled.Comment` | `Messages` | 可迁（近似） |
| `Default.MoreHoriz` / `Rounded.MoreVert` | `More` | 可迁 |
| `Default.EditCalendar` | `Edit` | 可迁（近似） |
| `Default.EmojiEmotions` | （无） | 暂留 / issue |
| `Outlined.WatchLater` / `Rounded.WatchLater` / `Rounded.Schedule` | `Alarm` / `Timer` | 可迁 |
| `Rounded.AccountCircle` | `ContactsCircle` | 可迁（近似） |
| `Rounded.Add` | `Add` | 可迁 |
| `Rounded.AddLink` | `Link` | 可迁 |
| `Rounded.ArrowDropDown` | `ExpandMore` | 可迁 |
| `Rounded.ArrowUpward` | （无） | 暂留 / issue |
| `Rounded.AutoAwesome` | （无） | 暂留 / issue |
| `Rounded.Block` | `Blocklist` | 可迁 |
| `Rounded.Book` | `Notes` | 可迁（近似） |
| `Rounded.Bookmark` / `BookmarkBorder` / `Bookmarks` | （无 bookmark） | 暂留 / issue |
| `Rounded.CalendarMonth` | `Months` | 可迁 |
| `Rounded.Check` | `Ok` 或 `MiuixIcons.Basic.Check` | 可迁 |
| `Rounded.CheckCircle` | `Ok` | 可迁 |
| `Rounded.Close` | `Close` 或 `MiuixIcons.Basic.Close` | 可迁 |
| `Rounded.CollectionsBookmark` | `MapAlbum` | 可迁（近似） |
| `Rounded.ContentCopy` | `Copy` | 可迁 |
| `Rounded.ContentPaste` | `Paste` | 可迁 |
| `Rounded.Delete` | `Delete` | 可迁 |
| `Rounded.Download` | `Download` | 可迁 |
| `Rounded.Equalizer` | `Tune` | 可迁 |
| `Rounded.Error` / `ErrorOutline` | `Report` | 可迁（近似） |
| `Rounded.Favorite` | `FavoritesFill` | 可迁 |
| `Rounded.FavoriteBorder` | `Favorites` | 可迁 |
| `Rounded.FilterAlt` / `FilterList` | `Filter` | 可迁 |
| `Rounded.Folder` | `Folder` | 可迁 |
| `Rounded.HideImage` | `Hide` | 可迁（近似） |
| `Rounded.History` | `Recent` | 可迁 |
| `Rounded.Home` | `Home` | 可迁 |
| `Rounded.Image` | `Image` | 可迁 |
| `Rounded.ImportExport` | `Import` | 可迁（近似） |
| `Rounded.Info` | `Info` | 可迁 |
| `Rounded.Lock` | `Lock` | 可迁 |
| `Rounded.NetworkWifi` | （无） | 暂留 / issue |
| `Rounded.Palette` | `Theme` | 可迁（近似） |
| `Rounded.Person` | `Contacts` | 可迁（近似） |
| `Rounded.PersonOff` | （无） | 暂留 / issue |
| `Rounded.Refresh` | `Refresh` | 可迁 |
| `Rounded.Save` | （无 save） | 暂留 / issue |
| `Rounded.Search` | `Search` 或 `MiuixIcons.Basic.Search` | 可迁 |
| `Rounded.Settings` | `Settings` | 可迁 |
| `Rounded.Storage` | （无） | 暂留 / issue |
| `Rounded.Style` | （无） | 暂留 / issue |
| `Rounded.Tag` | （无） | 暂留 / issue |
| `Rounded.TextFields` | （无） | 暂留 / issue |
| `Rounded.TouchApp` | （无） | 暂留 / issue |
| `Rounded.Translate` | `Translate` | 可迁 |
| `Rounded.Upload` | `UploadCloud` | 可迁（近似） |
| `Rounded.ViewModule` | `GridView` | 可迁 |
| `Rounded.Visibility` | `Show` | 可迁 |
| `Rounded.VisibilityOff` | `Hide` | 可迁 |
| `Rounded.Warning` | `Report` | 可迁（近似） |
| `Rounded._18UpRating` | （无） | 暂留 / issue |

Miuix basic icons（仅 Regular）：`ArrowRight`, `ArrowUpDown`, `Check`, `Close`, `Search`, `SearchCleanup`, `Sidebar` → use `MiuixIcons.Basic.X`.

## 6. 约束与风险

1. **minSdk 26** vs `miuix-blur` minSdk 33 → blur 需版本门控或放弃。
2. `androidx.compose.material3` 包下自定义文件（`IconButtonExt.kt`）**已删除**；长按按钮改为 `com.mrl.pixiv.common.compose.ui.LongPressIconButton`（foundation `combinedClickable`）。
3. Dynamic color + expressiveness 色板与 Miuix token 不一致，需 `ThemeController` 类桥接（skill: `ThemeController`, 深色模式, 动态颜色）。
4. Shared element + predictive back 依赖 Nav3 metadata，换组件库时勿改 graph 结构。
5. 双 material 栈（androidx + jetbrains）并存；Android-only 后可逐步收敛到单栈，但与 miuix 迁移解耦处理。
