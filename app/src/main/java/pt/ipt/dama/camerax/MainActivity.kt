package pt.ipt.dama.camerax

import android.Manifest
import android.content.ContentValues
import android.content.pm.PackageManager
import android.os.Build
import android.os.Bundle
import android.provider.MediaStore
import android.util.Log
import android.widget.Toast
import androidx.activity.enableEdgeToEdge
import androidx.activity.result.contract.ActivityResultContracts
import androidx.appcompat.app.AppCompatActivity
import androidx.camera.core.CameraSelector
import androidx.camera.core.ImageCapture
import androidx.camera.core.ImageCaptureException
import androidx.camera.core.Preview
import androidx.camera.lifecycle.ProcessCameraProvider
import androidx.core.content.ContextCompat
import androidx.core.view.ViewCompat
import androidx.core.view.WindowInsetsCompat
import pt.ipt.dama.camerax.databinding.ActivityMainBinding
import java.text.SimpleDateFormat
import java.util.Locale
import java.util.concurrent.ExecutorService
import java.util.concurrent.Executors

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


    /**
     * 'tira' a fotografia e guarda-a no espaço de armazenamento
     */
    private fun takePhoto() {
        // Get a stable reference of the modifiable image capture use case
        val imageCapture = imageCapture

        // Criar o nome a atribuir à imagem, à fotografia.
        // a este nome irá está associada a data e a hora da captura
        // import java.text.SimpleDateFormat
        // import java.util.Locale
        val name = SimpleDateFormat(FILENAME_FORMAT, Locale.US).format(System.currentTimeMillis())
        // definir o tipo de imagem e onde será guardada
        // https://developer.mozilla.org/en-US/docs/Web/HTTP/Guides/MIME_types
        // import android.content.ContentValues
        // import android.provider.MediaStore
        val contentValues = ContentValues().apply {
            put(MediaStore.MediaColumns.DISPLAY_NAME, name)
            put(MediaStore.MediaColumns.MIME_TYPE, "image/jpeg")
            if (Build.VERSION.SDK_INT > Build.VERSION_CODES.P) {
                // local onde o ficheiro vai ser guardado
                put(MediaStore.Images.Media.RELATIVE_PATH, "Pictures/CameraX-Images")
            }
        }

        // Create output options object which contains file + metadata
        val outputOptions = ImageCapture.OutputFileOptions.Builder(
            contentResolver, MediaStore.Images.Media.EXTERNAL_CONTENT_URI, contentValues
        ).build()

        // Set up image capture listener, which is triggered after photo has
        // been taken
        imageCapture.takePicture(
            outputOptions,
            ContextCompat.getMainExecutor(this),
            object : ImageCapture.OnImageSavedCallback {
                // import androidx.camera.core.ImageCaptureException
                override fun onError(exc: ImageCaptureException) {
                    Log.e(TAG, getString(R.string.photo_capture_failed, exc.message), exc)
                }

                override fun onImageSaved(output: ImageCapture.OutputFileResults) {
                    val msg = getString(R.string.photo_capture_succeeded, output.savedUri)
                    Toast.makeText(baseContext, msg, Toast.LENGTH_LONG).show()
                    Log.d(TAG, msg)
                }
            })
    }


    /**
     * inicia a câmara
     */
    private fun startCamera() {
        val cameraProviderFuture = ProcessCameraProvider.getInstance(this)

        cameraProviderFuture.addListener({
            // Used to bind the lifecycle of cameras to the lifecycle owner
            val cameraProvider: ProcessCameraProvider = cameraProviderFuture.get()

            // Preview
            val preview = Preview.Builder()
                .build()
                .also {
                    it.surfaceProvider = viewBinding.viewFinder.surfaceProvider
                }

            imageCapture = ImageCapture.Builder().build()

            // Select back camera as a default
            val cameraSelector = CameraSelector.DEFAULT_BACK_CAMERA

            try {
                // Unbind use cases before rebinding
                cameraProvider.unbindAll()

                // Bind use cases to camera
                cameraProvider.bindToLifecycle(
                    this, cameraSelector, preview, imageCapture
                )

            } catch (exc: Exception) {
                Log.e(TAG, getString(R.string.use_case_binding_failed), exc)
            }

        }, ContextCompat.getMainExecutor(this))
    }


    /**
     * devolve 'true' se houve autorização
     * devolve 'false' se foi negada a autorização
     */
    private fun allPermissionsGranted(): Boolean = REQUIRED_PERMISSIONS.all {
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
        registerForActivityResult(
            ActivityResultContracts.RequestMultiplePermissions()
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


    /**
     * por último, destruir o 'gestor da câmara'
     */
    override fun onDestroy() {
        super.onDestroy()
        cameraExecutor.shutdown()
    }


}
