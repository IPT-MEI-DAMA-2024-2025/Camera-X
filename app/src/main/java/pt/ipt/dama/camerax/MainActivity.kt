package pt.ipt.dama.camerax

import android.Manifest
import android.content.pm.PackageManager
import android.os.Build
import android.os.Bundle
import android.widget.Toast
import androidx.activity.enableEdgeToEdge
import androidx.activity.result.contract.ActivityResultContracts
import androidx.appcompat.app.AppCompatActivity
import androidx.camera.core.ImageCapture
import androidx.core.content.ContextCompat
import androidx.core.view.ViewCompat
import androidx.core.view.WindowInsetsCompat
import pt.ipt.dama.camerax.databinding.ActivityMainBinding
import java.util.concurrent.ExecutorService
import java.util.concurrent.Executors
import kotlin.math.sqrt

class MainActivity : AppCompatActivity() {

    /**
     * atributo que irá ser utilizado para aceder aos objetos da interface
     */
    private lateinit var viewBinding: ActivityMainBinding

    /**
     * manipulador da câmara
     */
    private lateinit var cameraExecutor: ExecutorService

    /**
     * manipulador da imagem obtida pela câmara
     */
    private lateinit var imageCapture: ImageCapture


    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        enableEdgeToEdge()
        setContentView(R.layout.activity_main)
        ViewCompat.setOnApplyWindowInsetsListener(findViewById(R.id.main)) { v, insets ->
            val systemBars = insets.getInsets(WindowInsetsCompat.Type.systemBars())
            v.setPadding(systemBars.left, systemBars.top, systemBars.right, systemBars.bottom)
            insets
        }

        // instanciar o manipulador dos objetos da interface
        // usando o ViewBinding, para não usar o 'findViewById'
        viewBinding = ActivityMainBinding.inflate(layoutInflater)
        setContentView(viewBinding.root)

        // avaliar se há autorização para aceder à câmara
        if (allPermissionsGranted()) {
            startCamera()
        } else {
            requestPermissions()
        }

        // Aceder ao botão, caso ele tenha sido pressionado
        viewBinding.imageCaptureButton.setOnClickListener {
            takePhoto()
        }

        // Configura o acesso à câmara
        // implementa o padrão 'singleton'
        cameraExecutor = Executors.newSingleThreadExecutor()

    }

    private fun takePhoto() {
        TODO("Not yet implemented")
    }

    /**
     * inicia a câmara
     */
    private fun startCamera() {
        TODO("Not yet implemented")
    }

    /**
     * devolve 'true' se houve autorização
     * devolve 'false' se foi negada a autorização
     */
    private fun allPermissionsGranted():Boolean = REQUIRED_PERMISSIONS.all {
        ContextCompat.checkSelfPermission(
            baseContext, it
        ) == PackageManager.PERMISSION_GRANTED
    }

    /**
     * pede autorização para usar os recursos
     */
    private fun requestPermissions() {
        activityResultLauncher.launch(REQUIRED_PERMISSIONS)
    }


    /**
     * quando é necessário pedir permissões
     * avalia a resposta do utilizador
     * e inicia o uso da câmara, ou informa que não é possível utilizá-la
     */
    private val activityResultLauncher =
        registerForActivityResult(  ActivityResultContracts.RequestMultiplePermissions()
        )
        { permissions ->

            // var que vai guardar a informação se a permissão foi concedida, ou não
            var permissionGranted = true
            permissions.entries.forEach {
                // testar para todos os tipos de permissões referido no Manifesto
                if (it.key in REQUIRED_PERMISSIONS && !it.value)
                    permissionGranted = false
            }
            if (!permissionGranted) {
                // há permissões que não foram concedidas
                Toast.makeText(
                    baseContext,
                    getString(R.string.permission_request_denied),
                    Toast.LENGTH_SHORT
                ).show()
            } else {
                startCamera()
            }
        }

    // https://kotlinlang.org/docs/object-declarations.html
    // https://medium.com/@appdevinsights/companion-object-in-kotlin-c3a1203cd63c
    companion object {
        private const val TAG = "CameraXApp"
        private const val FILENAME_FORMAT = "yyyy-MM-dd-HH-mm-ss-SSS"
        private val REQUIRED_PERMISSIONS =
            mutableListOf(
                // import android.Manifest
                Manifest.permission.CAMERA
            ).apply {
                if (Build.VERSION.SDK_INT <= Build.VERSION_CODES.P) {
                    add(Manifest.permission.WRITE_EXTERNAL_STORAGE)
                }
            }.toTypedArray()
    }





}
