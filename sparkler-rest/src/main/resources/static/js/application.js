"use strict";
import { Application } from "stimulus"
import HelloController from "./controllers/hello_controller"

const application = Application.start()
application.register("hello", HelloController)

const tagline = document.getElementById('tagline');
tagline.innerHTML = 'Added with JavaScript';
