package com.moviebooking.generator

import com.moviebooking.aggregates._

import scala.util.Random

object Generators {
  //  "<iframe width=\"560\" height=\"315\" src=\"https://www.youtube.com/embed/xjDjIWPwcPU\" frameborder=\"0\" allow=\"autoplay; encrypted-media\" allowfullscreen></iframe>"
  //<iframe width="560" height="315" src="https://www.youtube.com/embed/8BAhwgjMvnM" frameborder="0" allow="autoplay; encrypted-media" allowfullscreen></iframe>
  def theatres =
    List(("City Pride", Address("Seasons Mall", "Pune")),
         ("PVR Cinemas", Address("One Mall", "Pune")))
  def showTimes = List("09:30", "11:30", "03:15", "18:30", "21:30")
  def movies =
    List(
      MovieState(
        "Pacific Rim Uprising",
        List("John Boyega", "Burn Gorman"),
        "The globe-spanning conflict between otherworldly monsters of mass destruction and the human-piloted super-machines built to vanquish them was only a prelude to the all-out assault on humanity in Pacific Rim Uprising. \n\nJohn Boyega stars as the rebellious Jake Pentecost, a once-promising Jaeger pilot whose legendary father gave his life to secure humanity's victory against the monstrous \"Kaiju\". Jake has since abandoned his training only to become caught up in a criminal underworld. But when an even more unstoppable threat is unleashed to tear through our cities and bring the world to its knees, he is given one last chance to live up to his father's legacy by his estranged sister, Mako Mori -- who is leading a brave new generation of pilots that have grown up in the shadow of war. As they seek justice for the fallen, their only hope is to unite together in a global uprising against the forces of extinction. \n\nJake is joined by gifted rival pilot Lambert and 15-year-old Jaeger hacker Amara, as the heroes of the PPDC become the only family he has left. Rising up to become the most powerful defense force to ever walk the earth, they will set course for a spectacular all-new adventure on a towering scale.",
        "Action",
        Map("image" → "", "video" → "https://www.youtube.com/embed/xjDjIWPwcPU")
      ),
      MovieState(
        "Black Panther",
        List("Chadwick Boseman", "Lupita Nyong'o"),
        "After the events of Captain America: Civil War, King T'Challa returns home to the reclusive, technologically advanced African nation of Wakanda to serve as his country's new leader. However, T'Challa soon finds that he is challenged for the throne from factions within his own country. \n\nWhen two foes conspire to destroy Wakanda, the hero known as Black Panther must team up with C.I.A. agent Everett K. Ross and members of the Dora Milaje, Wakandan special forces, to prevent Wakanda from being dragged into a world war.",
        "Adventure",
        Map("image" → "", "video" → "https://www.youtube.com/embed/8BAhwgjMvnM")
      )
    )

  def generateShowIds = {
    val screenIds = (1 to 5).map(n ⇒ s"Screen${n}").toList
    screenIds.map(id ⇒
      ShowId(id, getRandom(showTimes), getRandom(theatreNames)))
  }

  private def theatreNames = {
    theatres.map(t ⇒ t._1).toList
  }

  def getRandom(list: List[String]) = {
    val randomIndex = new Random().nextInt(list.size)
    list(randomIndex)
  }

  def generateSeatMap = {
    val rowNumbers = 'A' to 'M'
    val seatNumbers = 1 to 20
    rowNumbers
      .flatMap(row ⇒ seatNumbers.map(no ⇒ SeatNumber(s"${row}", no)))
      .map(seatNo ⇒ Seat(seatNo))
      .toList
  }
}
