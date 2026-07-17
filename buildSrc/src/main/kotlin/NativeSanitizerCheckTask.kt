import org.gradle.api.DefaultTask
import org.gradle.api.GradleException
import org.gradle.api.provider.Property
import org.gradle.api.tasks.Input
import org.gradle.api.tasks.TaskAction

abstract class NativeSanitizerCheckTask : DefaultTask() {
    @get:Input
    abstract val sanitizersEnabled: Property<Boolean>

    @TaskAction
    fun verify() {
        if (!sanitizersEnabled.get()) {
            throw GradleException("Run nativeSanitizerCheck with -Papp.nativeSanitizers=true.")
        }
    }
}
