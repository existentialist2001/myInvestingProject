import org.apache.poi.ss.usermodel.*;
import org.apache.poi.xssf.usermodel.XSSFWorkbook;

import java.io.FileInputStream;
import java.io.FileOutputStream;
import java.io.IOException;
import java.time.LocalDate;
import java.time.format.DateTimeFormatter;
import java.util.List;

public class ExcelService {

    private static final String FilePath = "C:\\Users\\juson\\Desktop\\투자현황기록.xlsx";
    private static final DateTimeFormatter formatter = DateTimeFormatter.ofPattern("yyyy-MM-dd");

    public static void saveNewStockToExcel() {

        Stock stock = Portfolio.getPortfolio().get(0);

        //엑셀 읽기
        try (FileInputStream fis = new FileInputStream(FilePath);
             Workbook wb = new XSSFWorkbook(fis)) {

            Sheet sheet = wb.getSheetAt(0);
            int newRowNum = sheet.getLastRowNum() + 1;
            Row newRow = sheet.createRow(newRowNum);

            writeData(newRow,stock);

            //자동 열 너비 조정
            for (int i = 0; i < 4; i++) {
                sheet.autoSizeColumn(i);
            }

            //엑셀 쓰기
             try (FileOutputStream fos = new FileOutputStream(FilePath)) {

                 wb.write(fos);
                 System.out.println("엑셀 파일에 데이터가 저장되었습니다.");
             }
             catch (IOException e) {
                 System.out.println(e);
             }
        }
        catch (IOException e) {
            System.out.println(e);
        }

        Portfolio.getPortfolio().clear();
    }

    public static void initExcelAndSave() {

        Stock stock = Portfolio.getPortfolio().get(0);

        Workbook workbook = new XSSFWorkbook();
        Sheet sheet = workbook.createSheet("Stock Data");

        // 헤더 작성
        Row headerRow = sheet.createRow(0);
        headerRow.createCell(0).setCellValue("국가");
        headerRow.createCell(1).setCellValue("종목명");
        headerRow.createCell(2).setCellValue("평균 단가");
        headerRow.createCell(3).setCellValue("보유 주식 수");
        headerRow.createCell(4).setCellValue("최초 매수일");
        headerRow.createCell(5).setCellValue("총 매수액");

        // 데이터 작성
        Row dataRow = sheet.createRow(1);
        writeData(dataRow,stock);

        // 자동 열 너비 조정
        for (int i = 0; i < 4; i++) {
            sheet.autoSizeColumn(i);
        }

        // 파일 저장
        try (FileOutputStream fos = new FileOutputStream(FilePath)) {

            workbook.write(fos);
            System.out.println("엑셀 파일이 저장되었습니다: " + FilePath);
        } catch (IOException e) {
            System.out.println(e);
        }
        finally {

            try {
                workbook.close();
                Portfolio.getPortfolio().clear();

            } catch (IOException e) {
                System.out.println(e);
            }
        }
    }

    private static void writeData(Row dataRow, Stock stock) {

        dataRow.createCell(0).setCellValue(stock.getNation().name());
        dataRow.createCell(1).setCellValue(stock.getName());
        dataRow.createCell(2).setCellValue(stock.getAveragePrice());
        dataRow.createCell(3).setCellValue(stock.getHoldings());
        dataRow.createCell(4).setCellValue(stock.getFirstBuyDate().format(DateTimeFormatter.ISO_DATE));
        dataRow.createCell(5).setCellValue(stock.getTotalPrice());
    }

    public static void readStocksFromExcel() {

        try (FileInputStream fis = new FileInputStream(FilePath);
             Workbook workbook = new XSSFWorkbook(fis)) {

            Sheet sheet = workbook.getSheetAt(0);

            for (int rowIndex = 1; rowIndex <= sheet.getLastRowNum(); rowIndex++) {

                Row row = sheet.getRow(rowIndex);

                Nation nation = Nation.valueOf(row.getCell(0).getStringCellValue());
                String name = row.getCell(1).getStringCellValue();
                double averagePrice = row.getCell(2).getNumericCellValue();
                double holdings = row.getCell(3).getNumericCellValue();
                String firstBuyDate = row.getCell(4).getStringCellValue();
                LocalDate date = LocalDate.parse(firstBuyDate,formatter);

                Portfolio.getPortfolio().add(new Stock(nation,name,averagePrice,holdings,date));
            }
        }
        catch (IOException e) {
            System.out.println(e);
        }
    }

    public static int findStockFromExcel(String targetName) {

        int result = -1;

        try (FileInputStream fis = new FileInputStream(FilePath);
             Workbook wb = new XSSFWorkbook(fis)) {

            Sheet sheet = wb.getSheetAt(0);

            for (int rowNum = 1; rowNum <= sheet.getLastRowNum(); rowNum++) {

                Row row = sheet.getRow(rowNum);
                String stockName = row.getCell(1).getStringCellValue();

                if (stockName.equals(targetName)) {

                    Nation nation = Nation.valueOf(row.getCell(0).getStringCellValue());
                    double averagePrice = row.getCell(2).getNumericCellValue();
                    double holdings = row.getCell(3).getNumericCellValue();
                    String firstBuyDate = row.getCell(4).getStringCellValue();
                    LocalDate date = LocalDate.parse(firstBuyDate,formatter);
                    Portfolio.getPortfolio().add(new Stock(nation,stockName,averagePrice,holdings,date));

                    result = rowNum;
                    break;
                }
            }
        }
        catch (IOException e) {
            System.out.println(e);
        }
        return result;
    }

    public static void updatePreviousStock(int rowNum) {

        Stock stock = Portfolio.getPortfolio().get(0);

        try (FileInputStream fis = new FileInputStream(FilePath);
             Workbook wb = new XSSFWorkbook(fis);) {

            Sheet sheet = wb.getSheetAt(0);

            int avgPriceCellNum = 2;
            int holdingsCellNum = 3;
            int totalPirceCellNum = 5;

            Row row = sheet.getRow(rowNum);
            Cell avgPriceCell = row.getCell(avgPriceCellNum);
            Cell holdingsCell = row.getCell(holdingsCellNum);
            Cell totalPriceCell = row.getCell(totalPirceCellNum);

            avgPriceCell.setCellValue(stock.getAveragePrice());
            holdingsCell.setCellValue(stock.getHoldings());
            totalPriceCell.setCellValue(stock.getTotalPrice());

            try (FileOutputStream fos = new FileOutputStream(FilePath);) {

                wb.write(fos);
                System.out.println(stock.getName() + "의 정보가 업데이트 되었습니다.");
            }
        }
        catch (IOException e) {
            System.out.println(e);
        }

        Portfolio.getPortfolio().clear();
    }
}