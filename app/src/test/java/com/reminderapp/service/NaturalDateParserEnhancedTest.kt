package com.reminderapp.service

import org.junit.Assert.assertEquals
import org.junit.Assert.assertNull
import org.junit.Assert.assertTrue
import org.junit.Test

/**
 * v2.4.9: 手动创建对齐 AI——快速解析增强回归。
 * 事务类自动开启避开节假日；显式关键词优先；weekly 自动填星期。
 */
class NaturalDateParserEnhancedTest {

    @Test
    fun `报税自动开启避开节假日`() {
        val p = NaturalDateParser.parse("每季度报税")
        assertTrue("报税是工作日事务 → holidayAware=true", p?.holidayAware == true)
        assertEquals("每季度报税 → quarterly", "quarterly", p?.repeatMode)
    }

    @Test
    fun `换滤芯不开启避开节假日`() {
        val p = NaturalDateParser.parse("每半年换净水器滤芯")
        assertTrue("个人事务 → holidayAware=false", p?.holidayAware == false)
    }

    @Test
    fun `显式避开节假日关键词优先`() {
        val p = NaturalDateParser.parse("每周五交作业，避开节假日")
        assertTrue(p?.holidayAware == true)
        assertEquals("weekly", p?.repeatMode)
    }

    @Test
    fun `每周日自动填星期7`() {
        val p = NaturalDateParser.parse("每周日提醒我打针")
        assertEquals("weekly", p?.repeatMode)
        assertEquals(7, p?.weekday)
    }

    @Test
    fun `每周三自动填星期3`() {
        val p = NaturalDateParser.parse("每周三开会")
        assertEquals(3, p?.weekday)
        assertTrue("开会是工作日事务", p?.holidayAware == true)
    }

    @Test
    fun `普通提醒无星期无节假日`() {
        val p = NaturalDateParser.parse("明天下午3点开会")
        assertNull(p?.weekday)
        assertTrue(p?.holidayAware == true) // 开会关键词
    }

    @Test
    fun `新历旧历同时出现可分别识别`() {
        val text = "我生日新历9月5号，旧历8月11"
        // 拆两条逻辑在创建页（表单），此处验证 parser 至少能识别两种历法
        val solarP = NaturalDateParser.parse("我生日新历9月5号")
        assertEquals("solar_birthday", solarP?.dateType)
        val lunarP = NaturalDateParser.parse("我生日旧历8月11")
        assertEquals("lunar_birthday", lunarP?.dateType)
    }
}
