package excel;

import org.apache.poi.ss.usermodel.CellType;

public class ExportColumn {

    public enum DataType {

        none(CellType.STRING),
        date(CellType.STRING),
        percentage(CellType.NUMERIC),
        datetime(CellType.STRING),
        bool(CellType.BOOLEAN),
        currency(CellType.STRING),
        number(CellType.NUMERIC);

        private CellType cellType;

        DataType(CellType cellType) {
            this.cellType = cellType;
        }
    }
}
