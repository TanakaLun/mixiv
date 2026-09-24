# miuix Migration TODO

> 面向后续 agent 的执行清单。先读 skill **`miuix`**（Miuix 0.9.4，HyperOS 风格）。
> 组件对照与数据来源：`UI_STACK.md`。约定：conventional commits、英文 commit message、**不在本地编译**，推送后用 `gh` 追踪 CI。

## 0. 前置（必做）

- [x] 加载 `miuix` skill，核对当前版本 API（勿凭记忆写组件名）。
- [x] `gradle/libs.versions.toml` 增加 miuix 模块依赖（按需）：
  - `miuix-ui`、`miuix-preference`、`miuix-icons`、`miuix-nav`、`miuix-squircle`（原 `miuix-navigation3-ui` 已更名/弃用，现为自研 `miuix-nav`，零依赖 `androidx.navigation3`）
  - `miuix-blur`：仅当 minSdk≥33 或运行时门控；当前 **minSdk 26 → 默认不引入或加开关**。
- [x] 所有 Compose 根节点用 `MiuixTheme { ... }` 包裹（替换或桥接 `PiPixivTheme` / `MaterialExpressiveTheme`）。
- [ ] 确认 Overlay 组件调用链外层有 Miuix `Scaffold` 祖先（随 Phase 3 迁 preference/overlay 时确认）。
- [x] 决定策略：**增量（设置页先行）**，过渡期双栈。
- [ ] 每阶段结束：英文 conventional commit → push → `gh run list` / `gh run watch`。

## Phase 1 — 基础设施

- [x] 添加 miuix 依赖与 BOM/版本对齐（只动 catalog + 相关 `build.gradle.kts`）。
- [x] 新建 `ThemeController`（扩展现有 `Theme.kt`）：深色模式、动态色与 Miuix token 桥接（`SettingTheme` → `ColorSchemeMode.Monet*`）。
- [x] `PiPixivTheme` 内部改为 `MiuixTheme`；过渡期双栈（`MiuixTheme` 外包 `MaterialExpressiveTheme`，出口仍是单一 `PiPixivTheme`）。
- [x] 评估 `miuix-nav`：**本阶段不接入**，默认保留现有 AdaptiveScene / transitionSpec / SharedTransitionLayout / androidx.navigation3（catalog 已预留 `miuix-nav` 条目）。
- [ ] CI：确认 `develop.yml` 的 `android-compile`（无 secrets）在依赖变更后仍绿。

## Phase 2 — App Shell（主界面骨架）

- [ ] `feature/main/MainScreen.kt`：
  - [ ] `NavigationSuiteScaffold` → Miuix `NavigationBar` / `NavigationRail`（或保留 adaptive 型别决策 + 换控件）
  - [ ] Tab 内容切换动画保持 `fadeIn(220, delay 90)` / `fadeOut(90)` 或改为 Miuix 推荐 spec（需记录差异）
  - [ ] `Scaffold` → Miuix `Scaffold`
- [ ] 全局 `TopAppBar` 模式抽成共享组件（Miuix `TopAppBar`），先改 Main 下顶栏，再批量替换 36 处文件。
- [ ] `MainActivity` 动态取色逻辑：Miuix 是否支持 HyperOS 动色；不支持则固定品牌色并记录。

## Phase 3 — 设置与偏好（miuix-preference）

模块：`feature/setting`（约 15+ Screen）。

- [ ] 梳理所有 `ListItem` + `Switch` / `Arrow` 行 → `SwitchPreference` / `ArrowPreference` / 等价 preference。
- [ ] `AlertDialog` → `OverlayDialog`；确认调用方不在无 Scaffold 上下文。
- [ ] `ModalBottomSheet` → Miuix 底部 Overlay 体系。
- [ ] `DropDownSelector`、`EditDialog`、`BypassSettingEditor` 等组件迁 preference/overlay 样式。
- [ ] `AboutScreen` markdown-m3：保留渲染库，仅换容器 chrome（或后续换 Miuix 排版）。
- [ ] DatePicker / Slider / RadioButton：对照 skill 是否有 Miuix 版；无则暂留 Material 并标 TODO。

## Phase 4 — 业务屏（按模块）

优先级建议（高使用 → 低）：

- [ ] `feature/main`：Home / Ranking / Search / Latest / Profile / ProfileDetail
- [ ] `feature/picture` + `feature/image-preview`：大图、底栏、SnackBar/toast（sonner → 是否换 Miuix toast）
- [ ] `feature/login`：WebView 壳、按钮、进度
- [ ] `feature/collection`、`feature/follow`、`feature/history`
- [ ] `feature/novel`、`feature/comment`、`feature/report`、`feature/artwork`
- [ ] `composeApp`：`App.kt`、Splash、通用 Dialog/Snackbar

每屏检查清单：

- [ ] Theme 已是 `MiuixTheme` 路径
- [ ] 无新增 `androidx.compose.material3` import（存量逐步清）
- [ ] 图标：`Icons.Rounded.*` → `MiuixIcons`（82 个 import 清单见 `UI_STACK.md`）
- [ ] Ripple / pressed 态符合 Miuix
- [ ] 共享元素屏（IllustItem / Picture / ImagePreview）转场不回归

## Phase 5 — 图标与零碎 API

- [ ] 导出全部 material-icons import，建立 mapping 表；缺失图标记录 issue。
- [ ] 迁移或删除 `common/ui/.../androidx/compose/material3/IconButtonExt.kt`（**迁出 `androidx.compose.material3` 包**）。
- [ ] `ripple`、`IconButtonDefaults`、`CardDefaults`、`TextFieldDefaults` → Miuix 等价或原生。
- [ ] Wavy progress → Miuix progress。
- [ ] 评估移除 `material-icons-extended` 依赖（图标迁完后）。

## Phase 6 — 收尾与清理

- [ ] 删除不再使用的 material3 / adaptive 依赖（谨慎：adaptive NavigationSuite 若仍需要则保留）。
- [ ] 移除 `MaterialExpressiveTheme`、expressive color scheme 残留（确认无动态色依赖）。
- [ ] 更新 `UI_STACK.md`（迁移后状态）与本文件勾选。
- [ ] README / 截图（若需要，用户明确要求时再做）。
- [ ] 最终 CI：`android-compile` 必绿；有 secrets 时 `android-foss` 绿；`gh run watch`。

## 非目标 / 明确不做

- 不在本地执行 Gradle 编译/测试（用户要求只靠 CI 验证）。
- 不改 Navigation3 图结构、Destination、Shared element key（除非单独任务）。
- 不引入 desktop/iOS 已裁剪目标的回潮。
- 不默认开启 `miuix-blur`（minSdk 26）。

## 建议 Commit 切分（英文、conventional）

1. `build: add miuix dependencies and theme entry`
2. `feat: migrate app shell to miuix navigation and scaffold`
3. `feat: migrate settings screens to miuix preference`
4. `feat: migrate feature screens to miuix components`
5. `refactor: replace material icons with miuix icons`
6. `chore: drop unused material3 dependencies`

（每步可再按 module 拆分；push 到 `feature/**` 触发 `develop.yml`。）

## 快速验证命令

```bash
gh run list --limit 10
gh run watch <run-id>
gh run view <run-id> --log-failed
```
