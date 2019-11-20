package android.rmit.androidass2;

public class DataReport {
    private int amountOfGarbage = 0;
    private String id;

    public DataReport() {
    }

    public DataReport(int amountOfGarbage) {
        this.amountOfGarbage = amountOfGarbage;
    }

    public String getId() {
        return id;
    }

    public void setId(String id) {
        this.id = id;
    }



    public int getAmountOfGarbage() {
        return amountOfGarbage;
    }

    public void setAmountOfGarbage(int amountOfGarbage) {
        this.amountOfGarbage = amountOfGarbage;
    }

    @Override
    public String toString() {
        return "DataReport{" +
                "amountOfGarbage=" + amountOfGarbage +
                ", id='" + id + '\'' +
                '}';
    }
}
