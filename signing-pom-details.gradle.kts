apply(plugin = "maven-publish")
apply(plugin = "signing")

repositories {
   mavenCentral()
}

val signingKey: String? by project
val signingPassword: String? by project

fun Project.publishing(action: PublishingExtension.() -> Unit) =
   configure(action)

fun Project.signing(configure: SigningExtension.() -> Unit): Unit =
   configure(configure)

val publications: PublicationContainer = (extensions.getByName("publishing") as PublishingExtension).publications

signing {
   useGpgCmd()
   if (signingKey != null && signingPassword != null) {
      @Suppress("UnstableApiUsage")
      useInMemoryPgpKeys(signingKey, signingPassword)
   }
   if (Ci.isRelease) {
      sign(publications)
   }
}

publishing {
   repositories {
      maven {
         val releasesRepoUrl = uri("https://oss.sonatype.org/service/local/staging/deploy/maven2/")
         val snapshotsRepoUrl = uri("https://s01.oss.sonatype.org/content/repositories/snapshots/")
         name = "deploy"
         url = if (Ci.isRelease) releasesRepoUrl else snapshotsRepoUrl
         credentials {
            username = java.lang.System.getenv("OSSRH_USERNAME") ?: ""
            password = java.lang.System.getenv("OSSRH_PASSWORD") ?: ""
         }
      }
   }

   publications.withType<MavenPublication>().forEach {
      it.apply {
         //if (Ci.isRelease)
         pom {
            name.set("Kotest")
            description.set("Kotest assertions for Ktor")
            url.set("http://www.github.com/kotest/kotest-assertions-ktor")

            scm {
               connection.set("scm:git:http://www.github.com/kotest/kotest-assertions-ktor")
               developerConnection.set("scm:git:http://github.com/sksamuel/")
               url.set("http://www.github.com/kotest/kotest-assertions-ktor")
            }

            licenses {
               license {
                  name.set("Apache-2.0")
                  url.set("https://opensource.org/licenses/Apache-2.0")
               }
            }

            developers {
               developer {
                  id.set("sksamuel")
                  name.set("Stephen Samuel")
                  email.set("sam@sksamuel.com")
               }
            }
         }
      }
   }
}
