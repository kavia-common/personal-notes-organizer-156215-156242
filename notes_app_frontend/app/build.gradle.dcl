androidApplication {
    namespace = "org.example.app"

    dependencies {
        // AndroidX core and UI
        implementation("androidx.core:core-ktx:1.13.1")
        implementation("androidx.appcompat:appcompat:1.7.0")
        implementation("com.google.android.material:material:1.12.0")
        implementation("androidx.recyclerview:recyclerview:1.3.2")
        implementation("androidx.constraintlayout:constraintlayout:2.1.4")
        implementation("androidx.drawerlayout:drawerlayout:1.2.0")
        implementation("androidx.coordinatorlayout:coordinatorlayout:1.2.0")
    }

    // Configure unit test dependencies using the declarative DSL
    testing {
        dependencies {
            // JUnit 4 API (for tests written with JUnit 4)
            implementation("junit:junit:4.13.2")
            // Vintage engine to run JUnit 4 on the JUnit Platform (AGP integrates with Gradle test tasks)
            implementation("org.junit.vintage:junit-vintage-engine:5.10.2")
        }
    }
}
