package itmo.is.cw.GuitarMatchIS.models;

public enum ProductSort {
   NAME, RATE, PRICE;

   public String getFieldName() {
       return switch (this) {
           case NAME -> "name";
           case RATE -> "rate";
           case PRICE -> "avgPrice";
       };
   }
}
