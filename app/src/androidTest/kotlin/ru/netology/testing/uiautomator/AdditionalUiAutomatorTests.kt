package ru.netology.testing.uiautomator

import android.content.Context
import android.content.Intent
import androidx.test.core.app.ApplicationProvider
import androidx.test.ext.junit.runners.AndroidJUnit4
import androidx.test.platform.app.InstrumentationRegistry
import androidx.test.uiautomator.By
import androidx.test.uiautomator.UiDevice
import androidx.test.uiautomator.Until
import org.junit.After
import org.junit.Assert.assertEquals
import org.junit.Before
import org.junit.Test
import org.junit.runner.RunWith

@RunWith(AndroidJUnit4::class)
class AdditionalUiAutomatorTests {

    private lateinit var device: UiDevice
    private val timeout = 7_000L

    private val context: Context by lazy { ApplicationProvider.getApplicationContext() }
    private val appPackage: String by lazy { context.packageName }

    // ID элементов из проекта
    private val editTextId = "userInput"
    private val mainTextViewId = "textToBeChanged"
    private val changeButtonId = "buttonChange"
    private val openActivityButtonId = "buttonActivity"
    private val secondActivityTextViewId = "text" // TextView во второй Activity

    @Before
    fun setUp() {
        device = UiDevice.getInstance(InstrumentationRegistry.getInstrumentation())

        // Стартуем приложение с очисткой back stack
        val intent = context.packageManager.getLaunchIntentForPackage(appPackage)!!.apply {
            addFlags(Intent.FLAG_ACTIVITY_CLEAR_TASK)
        }
        context.startActivity(intent)

        // Ждём пока окно приложения станет активным
        device.wait(Until.hasObject(By.pkg(appPackage).depth(0)), timeout)
        device.waitForIdle()
    }

    @After
    fun tearDown() {
        // Возвращаемся на рабочий стол (необязательно)
        device.pressHome()
    }

    /**
     * ТЕСТ 1: Пустой ввод (включая строку из пробелов) не должен менять текст в TextView.
     */
    @Test
    fun emptyOrSpacesInputDoesNotChangeMainText() {
        val edit = device.wait(Until.findObject(By.res(appPackage, editTextId)), timeout)
            ?: throw AssertionError("EditText '$editTextId' не найден")
        val changeBtn = device.findObject(By.res(appPackage, changeButtonId))
            ?: throw AssertionError("Кнопка '$changeButtonId' не найдена")
        val mainText = device.findObject(By.res(appPackage, mainTextViewId))
            ?: throw AssertionError("TextView '$mainTextViewId' не найден")

        val initial = mainText.text

        // Пустая строка
        edit.text = ""
        changeBtn.click()
        device.waitForIdle()

        assertEquals(
            "Текст изменился при пустом вводе, а должен остаться прежним",
            initial,
            mainText.text
        )

        // Строка из пробелов
        edit.text = "    "
        changeBtn.click()
        device.waitForIdle()

        assertEquals(
            "Текст изменился при вводе только пробелов, а должен остаться прежним",
            initial,
            mainText.text
        )
    }

    /**
     * ТЕСТ 2: Открываем вторую Activity и сверяем текст.
     */
    @Test
    fun openSecondActivityAndCompareText() {
        val input = "Netology QA 2.2"

        val edit = device.wait(Until.findObject(By.res(appPackage, editTextId)), timeout)
            ?: throw AssertionError("EditText '$editTextId' не найден")
        val openBtn = device.findObject(By.res(appPackage, openActivityButtonId))
            ?: throw AssertionError("Кнопка '$openActivityButtonId' не найдена")

        // Вводим непустой текст
        edit.text = input

        // Открываем вторую Activity
        openBtn.click()

        // Ждём появления TextView со вторым экраном
        device.wait(Until.hasObject(By.res(appPackage, secondActivityTextViewId)), timeout)
        val secondText = device.findObject(By.res(appPackage, secondActivityTextViewId))
            ?: throw AssertionError("TextView второй Activity '$secondActivityTextViewId' не найден")

        assertEquals(
            "Текст во второй Activity не совпал с введённым",
            input,
            secondText.text
        )
    }
}
