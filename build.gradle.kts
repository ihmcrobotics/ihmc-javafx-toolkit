plugins {
   id("us.ihmc.ihmc-build")
   id("us.ihmc.ihmc-ci") version "8.3"
   id("us.ihmc.ihmc-cd") version "1.26"
}

ihmc {
   group = "us.ihmc"
   version = "17-0.22.5"
   vcsUrl = "https://github.com/ihmcrobotics/ihmc-javafx-toolkit"
   openSource = true
   
   configureDependencyResolution()
   configurePublications()
}

mainDependencies {
   api("net.sf.trove4j:trove4j:3.0.3")

   api("org.fxyz3d:fxyz3d:0.1.1") {
      exclude(group = "java3d", module = "vecmath")
      exclude(group = "org.slf4j", module = "slf4j-simple")
   }

   api("us.ihmc:euclid:0.20.0")
   api("us.ihmc:ihmc-graphics-description:0.20.5")
   api("us.ihmc:jassimp:4.0.0-ihmc6")

   var javaFXVersion = "17.0.2"
   api(ihmc.javaFXModule("base", javaFXVersion))
   api(ihmc.javaFXModule("controls", javaFXVersion))
   api(ihmc.javaFXModule("graphics", javaFXVersion))
   api(ihmc.javaFXModule("fxml", javaFXVersion))
   api(ihmc.javaFXModule("swing", javaFXVersion))
}

visualizersDependencies {
   api(ihmc.sourceSetProject("main"))
}
