import org.apache.commons.lang3.SystemUtils

buildscript {
   dependencies {
      classpath("org.apache.commons:commons-lang3:3.9")
   }
}

plugins {
   id("us.ihmc.ihmc-build") version "0.20.1"
   id("us.ihmc.ihmc-ci") version "5.3"
   id("us.ihmc.ihmc-cd") version "1.8"
}

ihmc {
   group = "us.ihmc"
   version = "0.14.0"
   vcsUrl = "https://github.com/ihmcrobotics/ihmc-javafx-toolkit"
   openSource = true
   
   configureDependencyResolution()
   configurePublications()
}

mainDependencies {
   api("net.sf.trove4j:trove4j:3.0.3")
   api("org.apache.commons:commons-lang3:3.9")

   api("org.fxyz3d:fxyz3d:0.1.1") {
      exclude(group = "java3d", module = "vecmath")
   }

   api("us.ihmc:euclid:0.12.2")
   api("us.ihmc:ihmc-yovariables:0.4.0")
   api("us.ihmc:ihmc-messager:0.1.3")
   api("us.ihmc:ihmc-graphics-description:0.14.0")
   api("us.ihmc:jassimp:4.0.0-ihmc5")

   api("org.openjfx:javafx-base:13.0.1:win")
   api("org.openjfx:javafx-controls:13.0.1:win")
   api("org.openjfx:javafx-graphics:13.0.1:win")
   api("org.openjfx:javafx-graphics:13.0.1:mac")
   api("org.openjfx:javafx-graphics:13.0.1:linux")
   api("org.openjfx:javafx-fxml:13.0.1:win")
   api("org.openjfx:javafx-swing:13.0.1:win")
}

visualizersDependencies {
   api(ihmc.sourceSetProject("main"))
}
