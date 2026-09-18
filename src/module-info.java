module assignment2dist {
	requires javafx.controls;
	requires org.zeromq.jeromq;
	
	opens application to javafx.graphics, javafx.fxml;
}
