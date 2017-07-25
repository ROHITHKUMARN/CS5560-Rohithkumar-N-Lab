package controllers

import javax.inject.Inject

import play.api.mvc.{AbstractController, ControllerComponents}

/**
  * Created by rohithkumar on 7/23/17.
  */
class AnswerRetrieval @Inject()(cc: ControllerComponents, s: String) extends AbstractController(cc)  {

 def question(s: String) = Action{
  Ok(views.html.myfinalpage(s))
 }
}
