import com.psy.deardiary.data.repository.AuthRepository
import com.psy.deardiary.data.repository.Result
import com.psy.deardiary.data.datastore.UserPreferencesRepository
import com.psy.deardiary.data.dto.TokenResponse
import com.psy.deardiary.data.dto.UserProfileResponse
import com.psy.deardiary.data.network.AuthApiService
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
import org.mockito.kotlin.any
import org.mockito.kotlin.mock
import org.mockito.kotlin.verify
import org.mockito.kotlin.whenever
import retrofit2.Response
import kotlin.test.assertTrue

@OptIn(ExperimentalCoroutinesApi::class)
class AuthRepositoryTest {
    private val dispatcher = UnconfinedTestDispatcher()
    private lateinit var authApi: AuthApiService
    private lateinit var userApi: UserApiService
    private lateinit var prefs: UserPreferencesRepository
    private lateinit var repository: AuthRepository

    @Before
    fun setup() {
        Dispatchers.setMain(dispatcher)
        authApi = mock()
        userApi = mock()
        prefs = mock()
        repository = AuthRepository(authApi, userApi, prefs)
    }

    @After
    fun tearDown() {
        Dispatchers.resetMain()
    }

    @Test
    fun loginSuccess_returnsSuccess() = runTest {
        whenever(authApi.login(any())).thenReturn(Response.success(TokenResponse("t", "bearer")))
        whenever(userApi.getProfile()).thenReturn(Response.success(UserProfileResponse(1, "e", "n", null)))

        val result = repository.login("e", "p")

        assertTrue(result is Result.Success)
        verify(prefs).saveAuthToken("t")
        verify(prefs).saveUserId(1)
    }

    @Test
    fun loginError_returnsError() = runTest {
        whenever(authApi.login(any())).thenReturn(Response.error(400, "".toResponseBody(null)))

        val result = repository.login("e", "p")

        assertTrue(result is Result.Error)
    }
}
