package org.springsource.crm.android.view;


public interface AsyncActivity {


    public void showLoadingProgressDialog();

    public void showProgressDialog(CharSequence message);

    public void dismissProgressDialog();

}
