package itmo.is.cw.GuitarMatchIS.models;

public enum TypeOfMusician {
   MUSICAL_PRODUCER("MUSICAL_PRODUCER"), GUITARIST("GUITARIST"), DRUMMER("DRUMMER"), BASSIST("BASSIST"),
   SINGER("SINGER"), RAPPER("RAPPER"), KEYBOARDIST("KEYBOARDIST");

   private final String capsValue;

   TypeOfMusician(String capsValue) {
      this.capsValue = capsValue;
   }

   public String getCapsValue() {
      return capsValue;
   }
}