package com.hxht.mobile.committee

import org.json.JSONObject
import org.junit.Test

import org.junit.Assert.*

/**
 * Example local unit test, which will execute on the development machine (host).
 *
 * See [testing documentation](http://d.android.com/tools/testing).
 */
class ExampleUnitTest {
    /**
     * JSON包含在Android SDK中，JUnit单元测试无法使用，会抛异常；但可以在AndroidTest中使用，如果要在Junit中使用，需要在App或Library项目的build.gradle中添加依赖：
     * testCompile files('libs/json.jar')
     */
    @Test
    fun addition_isCorrect() {
        assertEquals(4, 2 + 2)
        val demoStr = "{\"code\":0,\"msg\":\"调用成功\",\"data\":{\"id\":4,\"uid\":\"65bac1d870b24107a689962315940c15\",\"title\":\"洛川会议\",\"summary\":\"1937年8月22—25日，中国共产党在陕西省洛川县城北10公里处的红军指挥部驻地冯家村召开了中共中央政治局扩大会议，史称洛川会议。洛川会议是中国共产党在历史转折关头召开的一次重要会议。它制定了中国共产党的全面抗战路线，规定了中国共产党的基本任务和各项具体政策，为中国共产党和全国人民指明了抗战的正确方向。\",\"logo\":\"/api/logo/5\",\"status\":2,\"reserveStart\":\"2018-08-15 09:41:00\",\"reserveEnd\":\"2018-08-15 10:11:00\",\"start\":null,\"end\":null,\"room\":{\"id\":1,\"title\":\"研发部会议室\"},\"files\":[{\"id\":16,\"title\":\"catalina.out\",\"size\":\"407530\",\"type\":\"out\",\"url\":\"/api/download/16\"}],\"creator\":{\"id\":1,\"title\":\"super\"},\"dateCreated\":\"2018-08-02 16:27:04\"}}"

        val s = "[{\"id\":32,\"name\":\"是是是，赶紧买\"},{\"id\":33,\"name\":\"关我吊事\"}]";
        val result = JSONObject(demoStr)
//        val files = result["files"]
        val list = arrayListOf(s)

        print(list.size)
    }
}
