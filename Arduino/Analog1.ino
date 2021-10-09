void setup() {
  Serial.begin(1200);
  //pinMode(A0,INPUT); //connected to D7
  
}
int sig;
void loop() {
  sig=analogRead(A0);
  Serial.println(sig);
  
  
  }
