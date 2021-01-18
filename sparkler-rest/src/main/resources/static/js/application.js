"use strict";
import { Application } from "stimulus"
import HelloController from "./controllers/hello_controller"
import Turbo  from "@hotwired/turbo"

const application = Application.start()
application.register("hello", HelloController)
