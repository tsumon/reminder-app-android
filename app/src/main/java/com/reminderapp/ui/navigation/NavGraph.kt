package com.reminderapp.ui.navigation

import androidx.compose.runtime.Composable
import androidx.compose.runtime.rememberCoroutineScope
import androidx.navigation.NavHostController
import androidx.navigation.NavType
import androidx.navigation.compose.NavHost
import androidx.navigation.compose.composable
import androidx.navigation.navArgument
import com.reminderapp.data.database.AppDatabase
import com.reminderapp.service.NotificationManager
import com.reminderapp.service.ReminderScheduler
import com.reminderapp.ui.screen.CreateReminderScreen
import com.reminderapp.ui.screen.HomeScreen
import com.reminderapp.ui.screen.ReminderDetailScreen
import com.reminderapp.ui.viewmodel.HomeViewModel
import com.reminderapp.ui.viewmodel.ReminderDetailViewModel
import kotlinx.coroutines.launch

@Composable
fun NavGraph(
    navController: NavHostController,
    database: AppDatabase,
    scheduler: ReminderScheduler,
    notificationMgr: NotificationManager
) {
    NavHost(navController = navController, startDestination = "home") {

        composable("home") {
            val viewModel = HomeViewModel(
                dao = database.reminderDao(),
                recordDao = database.reminderRecordDao(),
                scheduler = scheduler
            )

            HomeScreen(
                viewModel = viewModel,
                onCreateReminder = { navController.navigate("create") },
                onReminderClick = { id -> navController.navigate("detail/$id") }
            )
        }

        composable("create") {
            val scope = rememberCoroutineScope()
            CreateReminderScreen(
                onSave = { entity ->
                    scope.launch {
                        val id = database.reminderDao().insert(entity)
                        val saved = entity.copy(id = id)
                        scheduler.schedule(saved)
                        navController.popBackStack()
                    }
                },
                onBack = { navController.popBackStack() }
            )
        }

        composable(
            route = "detail/{reminderId}",
            arguments = listOf(navArgument("reminderId") { type = NavType.LongType })
        ) { backStackEntry ->
            val reminderId = backStackEntry.arguments?.getLong("reminderId") ?: return@composable

            val viewModel = ReminderDetailViewModel(
                reminderId = reminderId,
                dao = database.reminderDao(),
                recordDao = database.reminderRecordDao(),
                scheduler = scheduler,
                notificationMgr = notificationMgr
            )

            ReminderDetailScreen(
                viewModel = viewModel,
                onBack = { navController.popBackStack() }
            )
        }
    }
}
