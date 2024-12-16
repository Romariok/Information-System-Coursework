package itmo.is.cw.GuitarMatchIS.models;

public enum MusicianSort {
   NAME, SUBSCRIBERS;

   public String getFieldName() {
       return switch (this) {
           case NAME -> "name";
           case SUBSCRIBERS -> "subscribers";
       };
   }
}
