package pt.ipt.dama.camerax

import android.os.Bundle
import androidx.activity.enableEdgeToEdge
import androidx.appcompat.app.AppCompatActivity
import androidx.camera.core.ImageCapture
import androidx.core.view.ViewCompat
import androidx.core.view.WindowInsetsCompat
import pt.ipt.dama.camerax.databinding.ActivityMainBinding
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
        if (allPremissionsGranted()) {
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

    private fun startCamera() {
        TODO("Not yet implemented")
    }

    private fun allPremissionsGranted(): Boolean {
        TODO("Not yet implemented")
    }
}
