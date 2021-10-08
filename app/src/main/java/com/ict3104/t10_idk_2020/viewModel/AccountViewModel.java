package com.ict3104.t10_idk_2020.viewModel;

import androidx.lifecycle.ViewModel;
import com.ict3104.t10_idk_2020.model.Account;
import com.ict3104.t10_idk_2020.repository.AccountRepository;
import com.ict3104.t10_idk_2020.repository.AccountRepository;
import com.ict3104.t10_idk_2020.repository.BooleanCallBack;
import com.ict3104.t10_idk_2020.repository.IntegerCallBack;

public class AccountViewModel extends ViewModel {

    private AccountRepository accountRepository = new AccountRepository();

    public void isUserExist(String email, String password, IntegerCallBack callBack){
        accountRepository.isUserExist(email, password, callBack);
    }
  
   public void isAccountExist(String email, IntegerCallBack callBack){
        accountRepository.isAccountExist(email, callBack);
    }

    public void insertAccount(String uid, Account account, BooleanCallBack callBack){
        accountRepository.insertAccount(uid, account, callBack);
    }

}
