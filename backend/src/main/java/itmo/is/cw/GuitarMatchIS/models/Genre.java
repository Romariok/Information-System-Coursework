package itmo.is.cw.GuitarMatchIS.models;

public enum Genre {
   BLUES("BLUES"), ROCK("ROCK"), POP("POP"), JAZZ("JAZZ"), RAP("RAP"), METAL("METAL"), CLASSICAL("CLASSICAL"),
   REGGAE("REGGAE"), ELECTRONIC("ELECTRONIC"), HIP_HOP("HIP_HOP");

   private final String capsValue;

   Genre(String capsValue) {
      this.capsValue = capsValue;
   }

   public String getCapsValue() {
      return capsValue;
   }
}