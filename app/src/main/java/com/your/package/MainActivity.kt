class MainActivity : AppCompatActivity() {
    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        setContentView(R.layout.activity_main)

        // Deshabilitar el back button
        onBackPressedDispatcher.addCallback(this, object : OnBackPressedCallback(true) {
            override fun handleOnBackPressed() {
                showExitDialog()
            }
        })

        // ... resto del código ...
    }

    private fun showExitDialog() {
        AlertDialog.Builder(this)
            .setTitle("¿Salir de la aplicación?")
            .setMessage("¿Estás seguro que deseas salir?")
            .setPositiveButton("Sí") { _, _ ->
                finish()
            }
            .setNegativeButton("No", null)
            .show()
    }

    @Deprecated("Deprecated in Java")
    override fun onBackPressed() {
        // No hacer nada aquí deshabilita el botón de retroceso
        // super.onBackPressed() // No llamar al super
    }

    // Para versiones más nuevas de Android (API 33+)
    override fun onBackPressedDispatcher() {
        // No hacer nada aquí
    }
} 