package net.sf.ij.vtk;

import vtk.vtkProcessObject;
import ij.IJ;

/**
 * Created by IntelliJ IDEA.
 * User: jarek
 * Date: Feb 5, 2003
 * Time: 10:13:00 PM
 * To change this template use Options | File Templates.
 */
public class VtkProgressObserver {
  vtkProcessObject processObject;

  public VtkProgressObserver(vtkProcessObject processObject) {
    this.processObject = processObject;
    processObject.SetProgressMethod(this, "progressMethod");
  }

  public void progressMethod() {
    double progress = processObject.GetProgress();
    String processObjectName = processObject.GetClassName();
    IJ.showStatus(processObjectName  + " " + (int)(progress*100)+"%");
    IJ.showProgress(progress);
//    System.out.println("Process object  : "+processObjectName);
//    System.out.println("Progress        : "+progress);
//    System.out.println("ProgressMaxValue: "+processObject.GetProgressMaxValue());
//    System.out.println("ProgressMinValue: "+processObject.GetProgressMinValue());
//    System.out.println("ProgressText    : "+processObject.GetProgressText());
  }
}
