package com.example.quiz15.service.impl;

import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.security.crypto.bcrypt.BCryptPasswordEncoder;
import org.springframework.stereotype.Service;

import com.example.quiz15.constants.ResCodMessage;
import com.example.quiz15.dao.UserDao;
import com.example.quiz15.entity.User;
import com.example.quiz15.service.ifs.UserService;
import com.example.quiz15.vo.AddInfoReq;
import com.example.quiz15.vo.BasicRes;
import com.example.quiz15.vo.LoginReq;

@Service
public class UserServiceImpl implements UserService {

	private BCryptPasswordEncoder encoder = new BCryptPasswordEncoder();

	@Autowired
	private UserDao userDao;

	@Override
	public BasicRes addInfo(AddInfoReq req) {
		// req 的參數檢查已經在類別 User 中透過 Validation 驗證
		// 1.檢查帳號是否已存在
		// 透過 PK email 去取得 count 數，只會得到 0 或 1
		int count = userDao.getCountByEmail(req.getEmail());
		if (count == 1) {// email 是 PK ，若 count = 1 表示帳號已存在
			return new BasicRes(ResCodMessage.EMAIL_EXISTS.getCode(), ResCodMessage.EMAIL_EXISTS.getMessage());
		}
		// 2.新增資訊
		try {
			userDao.addInfo(req.getName(), req.getPhone(), req.getEmail(), req.getAge(),
					encoder.encode(req.getPassword()));
			return new BasicRes(ResCodMessage.SUCCESS.getCode(), ResCodMessage.SUCCESS.getMessage());
		} catch (Exception e) {
			return new BasicRes(ResCodMessage.ADD_INFO_ERROR.getCode(), ResCodMessage.ADD_INFO_ERROR.getMessage());
		}
	}

	@Override
	public BasicRes login(LoginReq req) {
		//確認 email 是否存在於 DB
		User user =userDao.getByEmail(req.getEmail());
		if (user == null) {// email 是 PK ，若 count = 1 表示帳號已存在
			return new BasicRes(ResCodMessage.NOT_FOUND.getCode(), ResCodMessage.NOT_FOUND.getMessage());
		}
		//比對密碼
		//if 的條件式最前面有驚嘆號，等同於整個比對結果 == false 的意思
		if(!encoder.matches(req.getPassword(), user.getPassword())) {
			return new BasicRes(ResCodMessage.PASSWORD_MISMATCH.getCode(), ResCodMessage.PASSWORD_MISMATCH.getMessage());
		}
		return new BasicRes(ResCodMessage.SUCCESS.getCode(), ResCodMessage.SUCCESS.getMessage());
	}
	
}
