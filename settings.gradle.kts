// 国内镜像只在本地开发时用。
// CI 跑在 GitHub 海外 runner 上，走阿里云反而更慢且经常 502；
// 而 Gradle 对 502 是「致命错误」直接中断，不会像 404 那样 fallback 到下一个仓库，
// 所以不能靠「镜像在前、官方源在后」兜底，必须在 CI 上整个跳过镜像。
// 注意：pluginManagement 必须是本文件的第一个语句，
// 不能在它之前声明变量，所以下面直接内联读环境变量。
pluginManagement {
    repositories {
        if (System.getenv("CI") == null && System.getenv("GITHUB_ACTIONS") == null) {
            maven { url = uri("https://maven.aliyun.com/repository/google") }
            maven { url = uri("https://maven.aliyun.com/repository/public") }
            maven { url = uri("https://maven.aliyun.com/repository/gradle-plugin") }
        }
        google()
        mavenCentral()
        gradlePluginPortal()
    }
}
dependencyResolutionManagement {
    repositories {
        if (System.getenv("CI") == null && System.getenv("GITHUB_ACTIONS") == null) {
            maven { url = uri("https://maven.aliyun.com/repository/google") }
            maven { url = uri("https://maven.aliyun.com/repository/public") }
        }
        google()
        mavenCentral()
    }
}

rootProject.name = "ReminderApp"
include(":app")
