package PCS.PCSCore;


import java.util.Date;
import java.util.logging.*;

/**
 * Ticker Class for Driver to pick, pay fee and exit
 * @author ZHANG YIJIA
 */
public class Ticket {
    /**
     * Record Enter Time
     */
    protected final long enterTime;

    /**
     * Ticket ID
     */
    protected final int ticketID;

    //if smaller than 0, it means that it is not valid.

    /**
     * Driver should go out before Exit Time. if smaller than 0, it means that it is not valid.
     */
    protected long exitTime=-1;

    /**
     * Driver Parking fee. if smaller than 0, it means that it is not valid.
     */
    protected float parkingFee=-1;

    /**
     * Pay Machine ID for the Ticket. if empty, it means that it is not valid.
     */
    protected String payMachineID = "";

    /**
     * Set exitTime=exitTimeTmp when Oct Pay
     */
    protected long exitTimeTmp=-1;

    /**
     * Set parkingFee=parkingFeeTmp when Oct Pay
     */
    protected float parkingFeeTmp=-1;

    /**
     * Set payMachineID=payMachineIDTmp when Oct Pay
     */
    protected String payMachineIDTmp = "";

    /**
     * If true, it meas that the ticket already pay fee
     */
    protected boolean payJudge=false;

    /**
     * WIll be used for Ticket ID
     */
    protected static int TicketCount=0;

    public Ticket(){
        enterTime=System.currentTimeMillis();

        ticketID=TicketCount++;
    }

    /**
     * Calculate Parking Fee
     * @param coefficient Parking fee coefficient. By default, we set it to 5 in PCS.cfg file
     * Fee is calculated by second for easy demo
     * If there is no PayMachine set,which means this ticket isn't paid, so calculate the fee by time
     * If there is already a PayMachine ID here, which means the driver paid, then the fee is 0
     * Update the parkingFee
     * @return return parking fee
     * @author Pan Feng ZHANG YIJIA
     */
    public float calculateFee(float coefficient){
        long currentTime = System.currentTimeMillis();
        long time = currentTime-enterTime;//calculate the pay Fee time
        long currentSecond = time /1000 % 60;
        float parkingFeeCalculate = payMachineID.equals("")? coefficient * currentSecond : 0;
        return parkingFeeCalculate;
    }

    /**
     * Set Ticket ParyMachine ID
     * @param ID The PayMachine ID of the ticket
     *           @author ZHANG YIJIA
     */
    public void setPayMachineID(String ID){
        payMachineID=ID;
    }

    /**
     * Set Exit Time
     * @param exitTime exit Time
     *                 @author ZHANG YIJIA
     */
    public void setExitTime(long exitTime){
        this.exitTime=exitTime;
    }

    /**
     * Set parking fee
     * @param parkingFee The parking fee of the Ticket
     *                   @author ZHANG YIJIA
     */
    public void setParkingFee(float parkingFee) {
        this.parkingFee = parkingFee;
    }

    /**
     * Return Enter Time
     * @return Return Enter Time
     * @author ZHANG YIJIA
     */
    public long getEnterTime(){
        return enterTime;
    }

    /**
     * Return Parking fee
     * @return Return Parking Fee
     * @author Chuanyang Zheng
     */
    public float getParkingFee(){
        return parkingFee;
    }

    /**
     * Return Ticket ID
     * @return Return Ticket ID
     * @author ZHANG YIJIA
     */
    public int getTicketID() {
        return ticketID;
    }

    /**
     * Return Pay Machine ID
     * @return Pay Machine ID
     * @author ZHANG YIJIA
     */
    public String getPayMachineID() {
        return payMachineID;
    }

    /**
     * Return Exit Time
     * @return return Exit Time
     * @author ZHANG YIJIA
     */
    public long getExitTime() {
        return exitTime;
    }

    /**
     *
     * @param log The Logger From PCSCore
     * @param id The ID of PCSCore Will be used for log
     * @return If true, the ticket is valid. Other else, it is invalid.
     * @author Chuanyang Zheng
     */
    public boolean valid(Logger log,String id){
        if(ticketID<0)
        {
            log.warning(id+": "+ticketID+" is not valid");
            return false;
        }
        if( System.currentTimeMillis()>exitTime)
        {
            long currentTime=System.currentTimeMillis();
            log.warning(id+": "+"Current time"+PCSCore.getDate(new Date(currentTime))+"("+currentTime+") is larger than Exit time: "+PCSCore.getDate(new Date(exitTime))+"("+exitTime+")");
            return false;
        }
        if(payMachineID.equals(""))
        {
            log.warning(id+": Pay Machine ID is Emypy ["+payMachineID+"]");
            return false;
        }

        if( parkingFee<0){
            log.warning(id+": "+parkingFee+" is smaller than 0");
            return false;
        }

        payJudge=true;


        return true;
    }

    /**
     *set Ticket Information. It will be used after user giving money and pay Machine send the message to PCSCore
     *
     * @author Chuanyang Zheng
     */
    public void setExitInformation(){
        this.exitTime=this.exitTimeTmp;
        this.payMachineID=this.payMachineIDTmp;
        this.parkingFee=this.parkingFeeTmp;
    }

    /**
     * Store Tmp value when insert the ticket
     * @param exitTimeCoefficientTmp used to calculate exitTime=currentTime+exitTimeCoefficient Set exitTime=exitTimeTmp when Oct Pay
     * @param payMachineIDTmp set payMachine ID. Set payMachineID=payMachineIDTmp when Oct Pay
     * @param parkingFeeTmp calculated Parking fee. Set parkingFee=parkingFeeTmp when Oct Pay
     * @author Chuanyang Zheng
     */
    public void setExitInformationTmp(long exitTimeCoefficientTmp,String payMachineIDTmp, float parkingFeeTmp){
        this.exitTimeTmp=System.currentTimeMillis()+exitTimeCoefficientTmp;
        this.payMachineIDTmp=payMachineIDTmp;
        this.parkingFeeTmp=parkingFeeTmp;
    }

    /**
     * Get Parking Fee Tmp
     * @return parkingFeeTmp
     */
    public float getParkingFeeTmp(){
        return parkingFeeTmp;
    }

    public String toString(){
        return this.ticketID+" "+PCSCore.getDate(new Date(exitTime))+" "+parkingFee+" "+payMachineID+"\n";
    }
}
