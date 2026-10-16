# Reminder 2.8 / 提醒事项 2.8

版本 2.8.0（Android versionCode 70），支持 Android 8.0 / API 26 及以上。升级保留原应用 ID、正式签名、提醒、记录及现有功能。

## 日常使用

- 首页可输入一句话，使用原有离线时间解析器，然后进入完整编辑表单检查标题、日期、时间和重复规则后保存。复杂输入不确定时会提示改用手动表单或 AI；AI、语音、模板、批量管理仍保留。创建后可在首页撤销；有后续变更则拒绝撤销。
- 周期提醒可选择固定周期，或按实际完成时刻重新计时。日期、农历、节假日和第 N 周规则保留原有含义。固定周期是存量提醒的默认值。
- 轻柔提醒仅响一次，仍可手动完成。持续提醒保留原有 60、240、720、1440 分钟间隔与 5 次上限；可编辑间隔（1–10080 分钟、最多 10 项）和次数（1–10，含首次）。重要提醒使用原高优先级渠道。
- 所有重试遵守静默窗口。跳过本次不会增加完成次数；一次性提醒不能作为周期跳过。首页和详情提供撤销完成：仅恢复该次完成前状态、移除该条完成记录；后续编辑、动作或同步会使撤销失效。
- 首页优先显示待处理提醒，移除大型概览占位。详情可重新编辑完整提醒。

## 权限与可靠性

设置 → 提醒诊断包含当前通知权限、精确闹钟能力、权限设置入口和 1 分钟测试提醒。新建时也会说明当前能力。

定时提醒改用 AlarmManager：获得精确闹钟权限时使用 `setExactAndAllowWhileIdle`，否则使用 `setAndAllowWhileIdle`，并明确提示可能延迟。WorkManager 仍承担统计周报等延后后台任务。重启、时间/时区变化、应用更新和精确权限授予时恢复排期；权限撤销后系统会停止应用并取消精确闹钟，下次启动恢复为非精确排期。权限关闭时不会假装送达或耗尽提醒次数。

普通通知受通知渠道、静音、勿扰及系统电源策略影响。重要通知的全屏与勿扰权限由系统决定；不要将其理解为必定绕过静音/勿扰。可锁屏测试，不应只凭前台测试判断后台可达。

参考：[Android alarms](https://developer.android.com/develop/background-work/services/alarms)、[exact alarm permission](https://developer.android.com/about/versions/14/changes/schedule-exact-alarms)、[Android 16 SDK](https://developer.android.com/about/versions/16/setup-sdk)。

## 隐私与同步

诊断页提供隐私说明。提醒和完成记录存于本机；使用 AI 时，对话及相关提醒内容发往用户配置的模型服务；启用 WebDAV 后备份上传至用户配置服务器。下载目录内的本地 JSON 也包含提醒信息，应妥善保存。

双端同时修改时停止同步，在同步设置中明确选择保留本地上传或使用远程合并。自动同步冲突也会提示。选择前保存私有本地快照（`files/webdav_snapshots`，最近 5 份）；备份写入失败会停止操作。远程下载保留本地独有提醒及其排期；按稳定 syncId 更新已有项，保留本地 ID、完成记录、导入的稍后/重试状态。旧协议没有删除标记，删除不会传播。

上传使用 ETag / If-Match 检查远程版本；首次创建用 If-None-Match。服务器未提供可用 ETag 时，对已有文件的安全更新可能被拒绝并展示可操作错误，不会无条件覆盖。冲突选择若已被后续修改替代，要求重新同步再选择。

## 数据协议与开发

JSON schemaVersion 3；`version` 继续为 1，旧字段仍可读。新字段及缺省：

```json
{"recurrenceMode":"fixed","reminderStyle":"persistent","retryIntervalsMinutes":[60,240,720,1440],"maxReminderAttempts":5,"quietStartMinute":null,"quietEndMinute":null}
```

legacy isCritical=true 且没有 reminderStyle 时使用 important。静默字段为午夜起分钟数 0..1439，必须同时存在；无效间隔/次数回落旧策略。保留导入的 nextTriggerAt / retryCount / status；JSON 时间戳继续使用毫秒。共享行为数据见 `app/src/test/resources/fixtures/protocol_v3.json`，旧 v1/v2 fixture 继续测试。

跨端合并将标准 UUID 的大小写视为同一身份；保留已有本地数值 ID、syncId 原始写法、revision 递增及完成记录引用。稳定身份在整个集合中优先于旧指纹匹配；不同显式身份、非 UUID 的大小写不同标识仍分别保留。旧 iOS v1/v2 的 active 导入为 notifying，保留原 nextTriggerAt、retryCount、lastConfirmedAt；overdue 和其他状态不重新计算。

固定周期提前完成尚未触发的 pending/idle、retryCount=0 主提醒，会消耗该次未来周期一次。已触发重试或稍后的未来时间不作为周期锚点，避免跨过正常的下次主提醒。跳过使用相同区别；完成后重新计时的周期从实际跳过时刻加一个间隔，但不改 firstTriggerAt / lastConfirmedAt，也不计入完成次数。日期/规则的完成语义保持原状，跳过未来主提醒则前进一次。

Android `BackupService` 当前只导出提醒，不导出完成记录；保留本地记录不代表跨端历史同步。联调使用真实 Android 导出 → iOS `BackupHelper` 导入/导出 → Android 导入/导出/再导入，比较双方支持的身份、运行状态、策略、关键提醒和完成锚点字段；本地数值 ID、导出时间、UUID 大小写及平台私有字段不作等值要求。

Room 8→9 仅新增策略、静默和 revision 字段；旧关键提醒回填 important。保留原列、ID 和记录，迁移测试会比较每个旧列。SyncId 唯一索引与旧迁移保持一致，新建表也声明该索引。

构建：AGP 8.10.1、Gradle 8.11.1、JDK 17、compile/target SDK 36，minSdk 26。正式签名环境变量及默认 secret 保持原值，不生成新密钥。CI 的 PR 测试无需签名 secret，可信 push/dispatch 才生成正式 APK；v* tag 的原发布路径保留，本轮不打 tag。上传前核对证书 SHA-256 `0ea509a1dc9461c291a2db47caf5f055d83e4108fb28c3e87ed4fa62bead7038`、应用 ID 和版本。

```sh
./gradlew testDebugUnitTest assembleDebug lintDebug assembleDebugAndroidTest
./gradlew connectedDebugAndroidTest
```

Windows 的非 ASCII 路径检查已配置 override；若 JDK AF_UNIX 使用临时目录失败，可设置 `-Djdk.net.unixdomain.tmpdir=C:/codex-tools/tmp -Djava.io.tmpdir=C:/codex-tools/tmp`。该目录须先存在。

## 联调与发布证据边界

本轮增加真实导入/合并/引擎回归测试，并在 Room 动作测试中覆盖 UUID 合并后记录引用保留、提前完成/跳过及旧动作令牌拒绝。Windows 本地构建使用 ASCII 源码副本，每个受版本控制文件记录 SHA-256 对照；单元测试、debug APK、lint 和设备测试 APK 编译与最终 head 的 CI 分开报告。

API 29/36 CI 在产品设备测试后启动实际应用并截屏，随 `android-device-tests-api-*` 上传 `smoke/home.png`、启动日志和状态；截图仅为外观烟雾证据。包装脚本保留原设备测试退出码，截图失败会留下警告；设备测试失败仍令 CI 失败。签名验证继续只读取原有 CI secret，不生成替代密钥。

此前 Android `044fa34` 的 CI `38845115316` 单元测试与 API 29/36 设备测试通过，证书已与 v2.7.2 基线比较；这属于历史证据。新的联调提交仍须在准确 head 上重新运行完整 CI、签名 APK 的 `apksigner verify --print-certs` 与 `aapt dump badging`。最终 CI、产物及证书对照由 PR/联调报告记录，不能将历史通过视为新提交通过。

真机锁屏送达、Doze/OEM 后台行为、原正式签名 APK 的原地覆盖安装以及 iOS 分发签名仍未验证。iOS 逻辑/迁移、模拟器 UI 与 app/widget 归档须按各自准确提交和实际 CI 输出报告；本地 Windows 不能替代 Xcode 构建。

## 公开测试清单（待真机执行，不能视为已验证）

1. 从原正式签名 2.7.2 覆盖安装，确认旧周期、农历、节假日、规则与完成记录均存在。
2. 拒绝通知/精确权限，确认诊断显示实际状态；开启后安排 1 分钟测试，锁屏等待。关闭渠道不应耗尽次数。
3. 测试 Doze、进程回收、重启、改时区和权限撤销后重开，检查无重复和失去排期。
4. 在应用、通知与小组件分别完成/稍后，检查统计一致；旧通知不能操作后续周期。
5. 固定每天 09:00、11:00 完成应次日 09:00；完成后周期应次日 11:00。检查月末和夏令时。
6. 22:00–08:00 静默内主提醒、预告和重试延后；轻柔只响一次仍可完成。
7. 跳过不增加统计；撤销只移除对应完成记录；编辑/再次操作/同步后拒绝旧撤销。
8. WebDAV 双端冲突先停止；选择前本地快照存在；远端缺少的本地提醒仍可触发；旧协议不会传播删除。
9. 一句话输入、完整编辑、复杂规则退回、创建防重复、撤销、AI、语音、导入、分享与所有语言回归。
