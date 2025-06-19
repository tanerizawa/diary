import com.psy.deardiary.data.repository.UserRepository
import com.psy.deardiary.data.repository.Result
import com.psy.deardiary.data.dto.UserProfileResponse
import com.psy.deardiary.data.network.UserApiService
import kotlinx.coroutines.Dispatchers
import kotlinx.coroutines.ExperimentalCoroutinesApi
import kotlinx.coroutines.test.UnconfinedTestDispatcher
import kotlinx.coroutines.test.resetMain
import kotlinx.coroutines.test.runTest
import kotlinx.coroutines.test.setMain
import okhttp3.ResponseBody.Companion.toResponseBody
import org.junit.After
import org.junit.Before
import org.junit.Test
import org.mockito.kotlin.mock
import org.mockito.kotlin.whenever
import retrofit2.Response
import kotlin.test.assertTrue

@OptIn(ExperimentalCoroutinesApi::class)
class UserRepositoryTest {
    private val dispatcher = UnconfinedTestDispatcher()
    private lateinit var api: UserApiService
    private lateinit var repository: UserRepository

    @Before
    fun setup() {
        Dispatchers.setMain(dispatcher)
        api = mock()
        repository = UserRepository(api)
    }

    @After
    fun tearDown() {
        Dispatchers.resetMain()
    }

    @Test
    fun getProfileSuccess_returnsUser() = runTest {
        val profile = UserProfileResponse(1, "e", "n", null)
        whenever(api.getProfile()).thenReturn(Response.success(profile))

        val result = repository.getProfile()

        assertTrue(result is Result.Success)
    }

    @Test
    fun getProfileError_returnsError() = runTest {
        whenever(api.getProfile()).thenReturn(Response.error(404, "".toResponseBody(null)))

        val result = repository.getProfile()

        assertTrue(result is Result.Error)
    }
}
