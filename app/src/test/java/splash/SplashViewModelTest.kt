package splash

import android.content.Context
import com.example.myapplication.SplashScreenViewModel
import io.mockk.every
import io.mockk.mockk
import io.mockk.mockkStatic
import org.jsoup.Jsoup
import org.junit.jupiter.api.Test
import strikt.api.expectThat
import strikt.assertions.isEqualTo

class SplashViewModelTest {

    val context: Context = mockk {
        every { packageName } returns "packageName"
    }

    fun jsoupMockSetup(response: String?) {
        every { Jsoup.connect(any()) } returns mockk {
            every { timeout(any()) } returns this
            every { userAgent(any()) } returns this
            every { referrer(any()) } returns this
            every { get() } returns mockk {
                every { select(any()) } returns mockk {
                    val elm = this
                    every { elm[any()] } returns mockk {
                        every { ownText() } returns response
                    }
                }
            }
        }
    }

    val testObj = SplashScreenViewModel(context)

    @Test
    fun versionCheckActionTest() {
        mockkStatic(Jsoup::class) {
            val expected = "results"
            jsoupMockSetup(expected)

            val result = testObj.versionCheckAction()
            expectThat(result).isEqualTo(expected)
        }
    }

    @Test
    fun versionCheckActionThrowTest() {
        mockkStatic(
            Jsoup::class,
        ) {
            jsoupMockSetup(null) // pass null, to cause a "networking" exception

            val expected = "100"
            every { context.packageManager } returns mockk {
                every { getPackageInfo(any<String>(), any()) } returns mockk {
                    versionName = expected
                }
            }

            val result = testObj.versionCheckAction()
            expectThat(result).isEqualTo(expected)
        }
    }
}
