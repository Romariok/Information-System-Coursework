package itmo.is.cw.GuitarMatchIS.models;

public enum ArticleSort {
   CREATED_AT
   ;

   public String getFieldName() {
      return switch (this) {
         case CREATED_AT -> "createdAt";
      };
   }
}
