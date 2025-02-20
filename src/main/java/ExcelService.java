import org.apache.poi.ss.usermodel.*;
import org.apache.poi.xssf.usermodel.XSSFWorkbook;
import utility.apiRequest.Currency;

import java.io.FileInputStream;
import java.io.FileOutputStream;
import java.io.IOException;
import java.math.BigDecimal;
import java.math.RoundingMode;
import java.time.LocalDate;
import java.time.format.DateTimeFormatter;
import java.util.List;

public class ExcelService {

    private static final String FilePath = "C:\\Users\\juson\\Desktop\\투자현황기록.xlsx";
    private static final DateTimeFormatter formatter = DateTimeFormatter.ofPattern("yyyy-MM-dd");

    //ExcelService가 엑셀에서 데이터를 긁어와서 계산해서 제공하는 전체값
    //이러면 처음 프로그램이 시작하고 클래스가 로드될 때 한번 실행됨
    private static double totalPrice;

    public static void saveNewStockToExcel() {

        Stock stock = Portfolio.getPortfolio().get(0);


        //엑셀 읽기
        try (FileInputStream fis = new FileInputStream(FilePath);
             Workbook wb = new XSSFWorkbook(fis)) {

            Sheet sheet = wb.getSheetAt(0);
            int newRowNum = sheet.getLastRowNum() + 1;
            Row newRow = sheet.createRow(newRowNum);

            //가상 엑셀에 쓰기
            writeData(newRow,stock);

            //기존 주식들의 비중도 갱신된 총액에 따라 갱신하고, 써주기
            for (int i = 1; i < sheet.getLastRowNum() - 1; i++) {

                Row excelStock = sheet.getRow(i);
                BigDecimal eachTotalPrice = BigDecimal.valueOf(excelStock.getCell(5).getNumericCellValue());
                int newWeight = eachTotalPrice.divide(BigDecimal.valueOf(ExcelService.getTotalPrice()),0,RoundingMode.HALF_UP).intValue();
                Cell weightCell = excelStock.getCell(7);
                weightCell.setCellValue(newWeight);
            }

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
        //엑셀 갱신 후 전체값 갱신하는 작업
        updateTotalPrice();
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
        
        //추가
        headerRow.createCell(6).setCellValue("마지막 매수일");
        headerRow.createCell(7).setCellValue("비중");

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

    //엑셀에 데이터 쓰는 부분
    private static void writeData(Row dataRow, Stock stock) {

        dataRow.createCell(0).setCellValue(stock.getNation().name());
        dataRow.createCell(1).setCellValue(stock.getName());
        dataRow.createCell(2).setCellValue(stock.getAveragePrice());
        dataRow.createCell(3).setCellValue(stock.getHoldings());
        dataRow.createCell(4).setCellValue(stock.getFirstBuyDate().format(DateTimeFormatter.ISO_DATE));
        dataRow.createCell(5).setCellValue(stock.getTotalPrice());
        dataRow.createCell(6).setCellValue(stock.getLastBuyDate().format(DateTimeFormatter.ISO_DATE));
        dataRow.createCell(7).setCellValue(stock.getWeight());
    }

    //엑셀에서 다 긁어와서 stock 객체를 만들어준 후, Portfolio에 다 넣는 것
    public static void readStocksFromExcel() {

        try (FileInputStream fis = new FileInputStream(FilePath);
             Workbook workbook = new XSSFWorkbook(fis)) {

            Sheet sheet = workbook.getSheetAt(0);

            for (int rowIndex = 1; rowIndex <= sheet.getLastRowNum(); rowIndex++) {

                //row 하나가 주식 하나라고 보면 됨
                Row row = sheet.getRow(rowIndex);

                Nation nation = Nation.valueOf(row.getCell(0).getStringCellValue());
                String name = row.getCell(1).getStringCellValue();
                double averagePrice = row.getCell(2).getNumericCellValue();
                double holdings = row.getCell(3).getNumericCellValue();
                String firstBuyDate = row.getCell(4).getStringCellValue();
                LocalDate fdate = LocalDate.parse(firstBuyDate,formatter);
                String lastBuyDate = row.getCell(6).getStringCellValue();
                LocalDate ldate = LocalDate.parse(lastBuyDate,formatter);

                Portfolio.getPortfolio().add(new Stock(nation,name,averagePrice,holdings,fdate,ldate));
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

                    //똑같은 이름을 찾았으면, stock 객체 생성해서 portfolio에 넣어줌
                    Nation nation = Nation.valueOf(row.getCell(0).getStringCellValue());
                    double averagePrice = row.getCell(2).getNumericCellValue();
                    double holdings = row.getCell(3).getNumericCellValue();
                    String firstBuyDate = row.getCell(4).getStringCellValue();
                    LocalDate fdate = LocalDate.parse(firstBuyDate,formatter);
                    String lastBuyDate = row.getCell(6).getStringCellValue();
                    LocalDate ldate = LocalDate.parse(lastBuyDate,formatter);

                    Portfolio.getPortfolio().add(new Stock(nation,stockName,averagePrice,holdings,fdate,ldate));

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
            int lastBuyDateCellNum = 6;
            int weightCellNum = 7;

            Row row = sheet.getRow(rowNum);
            Cell avgPriceCell = row.getCell(avgPriceCellNum);
            Cell holdingsCell = row.getCell(holdingsCellNum);
            Cell totalPriceCell = row.getCell(totalPirceCellNum);
            Cell lastBuyDateCell = row.getCell(lastBuyDateCellNum);

            Cell weightCell = row.getCell(weightCellNum);
            if (weightCell == null) weightCell = row.createCell(weightCellNum);

            avgPriceCell.setCellValue(stock.getAveragePrice());
            holdingsCell.setCellValue(stock.getHoldings());
            totalPriceCell.setCellValue(stock.getTotalPrice());
            lastBuyDateCell.setCellValue(stock.getLastBuyDate().format(DateTimeFormatter.ISO_DATE));
            weightCell.setCellValue(stock.getWeight());

            //엑셀에 쓰기 전에 나머지 주식들도 비중 갱신
            //기존 주식들의 비중도 갱신된 총액에 따라 갱신하고, 써주기
        /*    for (int i = 1; i < sheet.getLastRowNum(); i++) {

                Row excelStock = sheet.getRow(i);
                BigDecimal eachTotalPrice = BigDecimal.valueOf(excelStock.getCell(5).getNumericCellValue());
                int newWeight = eachTotalPrice.divide(BigDecimal.valueOf(ExcelService.getTotalPrice()),0,RoundingMode.HALF_UP).intValue();
                Cell weightCell = excelStock.getCell(7);
                weightCell.setCellValue(newWeight);
            }*/


            try (FileOutputStream fos = new FileOutputStream(FilePath);) {

                wb.write(fos);
                System.out.println(stock.getName() + "의 정보가 업데이트 되었습니다.");
            }
        }
        catch (IOException e) {
            System.out.println(e);
        }

        Portfolio.getPortfolio().clear();
        //저장이 끝나면 총액 갱신
        ExcelService.updateTotalPrice();
    }

    public static int checkSameName(String name) {

        try (FileInputStream fis = new FileInputStream(FilePath);
             Workbook wb = new XSSFWorkbook(fis)) {

            Sheet sheet = wb.getSheetAt(0);

            for (int rowNum = 1; rowNum <= sheet.getLastRowNum(); rowNum++) {

                Row row = sheet.getRow(rowNum);
                String stockName = row.getCell(1).getStringCellValue();

                if (stockName.equals(name)) {
                    return -1;
                }
            }
        }
        catch (IOException e) {
            System.out.println(e);
        }
        return 1;
    }

    //엑셀에서 데이터 긁어와서, 전체 총액 계산(원화기준)
    private static double calculateTotalPrice() {

        BigDecimal bdtotalPrice = BigDecimal.valueOf(0);

        //원 달러 환율 받아오기
        //원 엔 환율 받아오기
        BigDecimal usd = BigDecimal.valueOf(Currency.getWonDollarExRate());
        System.out.println("usd = " + usd);
        BigDecimal jpy = BigDecimal.valueOf(Currency.getWonYenExRate());
        System.out.println("jpy = " + jpy);

        //데이터에서 총액과 국가만 긁어오면서 국가에 따라 환전하기
        try (FileInputStream fis = new FileInputStream(FilePath);
             Workbook workbook = new XSSFWorkbook(fis)) {

            Sheet sheet = workbook.getSheetAt(0);

            for (int rowIndex = 1; rowIndex <= sheet.getLastRowNum(); rowIndex++) {

                //row 하나가 주식 하나라고 보면 됨
                Row row = sheet.getRow(rowIndex);

                Nation nation = Nation.valueOf(row.getCell(0).getStringCellValue());
                BigDecimal averagePrice = BigDecimal.valueOf(row.getCell(2).getNumericCellValue());
                BigDecimal holdings = BigDecimal.valueOf(row.getCell(3).getNumericCellValue());
                BigDecimal temp = averagePrice.multiply(holdings).setScale(2,RoundingMode.HALF_UP);

                //환율 반영
                if (nation.getCurrency().equals("달러")) {

                    BigDecimal currencyTemp = temp.multiply(usd);
                    bdtotalPrice = bdtotalPrice.add(currencyTemp).setScale(2,RoundingMode.HALF_UP);
                }
                else if (nation.getCurrency().equals("엔")) {

                    BigDecimal currencyTemp = temp.multiply(jpy);
                   bdtotalPrice = bdtotalPrice.add(currencyTemp).setScale(2,RoundingMode.HALF_UP);
                }
                //원화일때는 그냥 더해주기
                else {
                    bdtotalPrice = bdtotalPrice.add(temp).setScale(2,RoundingMode.HALF_UP);
                }
            }
        }
        catch (IOException e) {
            System.out.println(e);
        }
        return bdtotalPrice.doubleValue();
    }

    public static double getTotalPrice() {
        return totalPrice;
    }

    //내가 원하시는 시점에 전체값을 업데이트하기 위한 함수
    public static void updateTotalPrice() {

        totalPrice = calculateTotalPrice();
        System.out.println("총액 계산 완료, 총액 = " + totalPrice);
    }

    public static void setTotalPrice(double newTotalPrice) {
        totalPrice = newTotalPrice;
    }

}