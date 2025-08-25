package com.example.quiz15.service.impl;

import java.time.LocalDate;
import java.util.ArrayList;
import java.util.List;

import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

import com.example.quiz15.constants.QuestionType;
import com.example.quiz15.constants.ResCodMessage;
import com.example.quiz15.dao.QuestionDao;
import com.example.quiz15.dao.QuizDao;
import com.example.quiz15.entity.Question;
import com.example.quiz15.entity.Quiz;
import com.example.quiz15.service.ifs.QuizService;
import com.example.quiz15.vo.BasicRes;
import com.example.quiz15.vo.QuestionRes;
import com.example.quiz15.vo.QuestionVO;
import com.example.quiz15.vo.QuizCreateReq;
import com.example.quiz15.vo.QuizUpdateReq;
import com.example.quiz15.vo.SearchReq;
import com.example.quiz15.vo.SearchRes;
import com.fasterxml.jackson.core.type.TypeReference;
import com.fasterxml.jackson.databind.ObjectMapper;

@Service
public class QuizServiceImpl implements QuizService {

	// 提供 類別(或Json 格式)與物件之間的轉換
	private ObjectMapper mapper = new ObjectMapper();

	@Autowired
	private QuizDao quizDao;

	@Autowired
	private QuestionDao questionDao;

	/**
	 * @throws Exception
	 * @Transactional: 事務</br>
	 *                 當一個方法中執行多個 Dao 時(跨表或是同一張表寫多筆資料)，這些所有的資料應該都要算同一次的行為，
	 *                 所以這些資料要嘛全部寫入成功，不然就全部寫入失敗 2. @Transactional 有效回朔的異常預設是
	 *                 RunTimeException，若發生的異常不是 RunTimeException
	 *                 或其子類別的異常類型，資料皆不會回朔，因此想要讓只要發生任何一種異常時資料都要可以回朔，可以
	 *                 將 @Transactional 的有效範圍從 RunTimeException 提高至 Exception
	 */
	@Transactional(rollbackFor = Exception.class)
	@Override
	public BasicRes create(QuizCreateReq req) throws Exception {
		// 參數檢查已透過 @Valid 驗證
		try {
			// 檢查日期:使用排除法
			BasicRes checkRes = checkDate(req.getStartDate(), req.getEndDate());
			if (checkRes.getCode() != 200) {// 不等於 200 表示檢查出有錯誤
				return checkRes;
			}
			// 新增問卷
			quizDao.insert(req.getName(), req.getDescription(), req.getStartDate(), //
					req.getEndDate(), req.isPublished());
			// 雖然因為 @Transactional 尚未將資料提交(commit)進資料庫，但實際上SQL語法已經執行完畢，
			// 依然可以取得對應的值
			int quizid = quizDao.getMaxQuizId();
			// 新增問題
			// 取出問卷中所有的問題
			List<QuestionVO> questionList = req.getQuestionList();
			// 處理每一個問題
			for (QuestionVO vo : questionList) {
				// 檢查題目類型與選項
				checkRes = checkQuestionType(vo);
				// 呼叫方法 checkQuestionType 得到的 res 若是 null，表示檢查都沒問題，
				// 因為方法中檢查到最後都沒問題時是回傳 null
				if (checkRes.getCode() != 200) {
					// return checkRes;
					// 因為前面已經執行了 quizDao.insert 了，所以這邊要拋出 Exception
					// 才會讓 @Transactional 生效
					throw new Exception(checkRes.getMessage());
				}
				// 因為 MySQL 沒有 List 的資料格式，所以要把 options 資料格式 從 List<String> 轉成 String
				List<String> optionList = vo.getOptions();
				String str = mapper.writeValueAsString(optionList);
				// 要記得設定 quizId
				questionDao.insert(quizid, vo.getQuestionId(), vo.getQuestion(), vo.getType(), vo.isRequired(), str);
			}
			return new BasicRes(ResCodMessage.SUCCESS.getCode(), ResCodMessage.SUCCESS.getMessage());
		} catch (Exception e) {
			// 不能 return BasicRes 而是要將發生的異常拋出去，這樣 @Transaction 才會生效
			throw e;
		}
	}

	private BasicRes checkQuestionType(QuestionVO vo) {
		// 檢查 type 是否是規定的類型
		String type = vo.getType();
		// 假設 從 vo 取出的 type 不符合定義的3種類型的其中一種，就返回錯誤訊息
		if (!type.equalsIgnoreCase(QuestionType.SINGLE.getType())
				&& !type.equalsIgnoreCase(QuestionType.MULTI.getType())//
				&& !type.equalsIgnoreCase(QuestionType.TEXT.getType())) {
			return new BasicRes(ResCodMessage.QUESTION_TYPE_ERROR.getCode(), //
					ResCodMessage.QUESTION_TYPE_ERROR.getMessage());
		}
		// 2. type 是單選或多選的時候，選項(options)至少要有2個
		// 假設 type 不等於 TEXT --> 就表示 type 是單選或多選
		if (!type.equalsIgnoreCase(QuestionType.TEXT.getType())) {
			// 單選或多選時，選項至少要有2個
			if (vo.getOptions().size() < 2) {
				return new BasicRes(ResCodMessage.OPTIONS_INSUFFICIENT.getCode(), //
						ResCodMessage.OPTIONS_INSUFFICIENT.getMessage());
			}
		} else { // else --> type 是 text --> 選項應該是 null 或是 size = 0
			if (vo.getOptions() != null && vo.getOptions().size() > 0) {
				return new BasicRes(ResCodMessage.TEXT_HAS_OPTIONS_ERROR.getCode(), //
						ResCodMessage.TEXT_HAS_OPTIONS_ERROR.getMessage());
			}
		}
		return new BasicRes(ResCodMessage.SUCCESS.getCode(), ResCodMessage.SUCCESS.getMessage());
	}

	private BasicRes checkDate(LocalDate starDate, LocalDate endDate) {
		// 1.開始日期不能比結束時間晚 2.開始日期不能比當前創建的日期早
		// 判斷式: 假設 開始時間比結束時間晚 或 開始時間比當前時間早 --> 回錯誤訊息
		// LocalDate.now() --> 取得當前的日期
		if (starDate.isAfter(endDate) || starDate.isBefore(LocalDate.now())) {
			return new BasicRes(ResCodMessage.DATE_FORMAT_ERROR.getCode(),
					ResCodMessage.DATE_FORMAT_ERROR.getMessage());
		}
		return new BasicRes(ResCodMessage.SUCCESS.getCode(), ResCodMessage.SUCCESS.getMessage());
	}

	@Transactional(rollbackFor = Exception.class)
	@Override
	public BasicRes update(QuizUpdateReq req) throws Exception {
		// 參數檢查已透過 @Valid 驗證
		// 更新是對已存在問卷
		try {
			// 1.檢查quizId是否存在
			int quizId = req.getQuizId();
			int count = quizDao.getCountByQuizId(req.getQuizId());
			if (count != 1) {
				return new BasicRes(ResCodMessage.NOT_FOUND.getCode(), //
						ResCodMessage.NOT_FOUND.getMessage());
			}
			// 2.檢查日期
			BasicRes checkRes = checkDate(req.getStartDate(), req.getEndDate());
			if (checkRes.getCode() != 200) {// 不等於 200 表示檢查出有錯誤
				return checkRes;
			}
			// 3.更新問卷
			int updateRes = quizDao.update(quizId, req.getName(), req.getDescription(), //
					req.getStartDate(), req.getEndDate(), req.isPublished());
			if (updateRes != 1) {// 表示資料沒更新成功
				return new BasicRes(ResCodMessage.QUIZ_UPDATE_FAILED.getCode(), //
						ResCodMessage.QUIZ_UPDATE_FAILED.getMessage());
			}
			// 4.刪除同一張問卷的所有問題
			questionDao.deleteByQuizId(quizId);
			// 5.檢查問題
			List<QuestionVO> questionList = req.getQuestionList();
			for (QuestionVO vo : questionList) {
				// 檢查題目類型與選項
				checkRes = checkQuestionType(vo);
				// 方法中檢查到最後都沒問題時是回傳成功
				if (checkRes.getCode() != 200) {
					// return checkRes;
					// 因為前面已經執行了 quizDao.insert 了，所以這邊要拋出 Exception
					// 才會讓 @Transactional 生效
					throw new Exception(checkRes.getMessage());
				}
				// 因為 MySQL 沒有 List 的資料格式，所以要把 options 資料格式 從 List<String> 轉成 String
				List<String> optionList = vo.getOptions();
				String str = mapper.writeValueAsString(optionList);
				// 要記得設定 quizId
				questionDao.insert(quizId, vo.getQuestionId(), vo.getQuestion(), vo.getType(), vo.isRequired(), str);
			}

		} catch (Exception e) {
			// 不能 return BasicRes 而是要將發生的異常拋出去，這樣 @Transaction 才會生效
			throw e;
		}
		return new BasicRes(ResCodMessage.SUCCESS.getCode(), ResCodMessage.SUCCESS.getMessage());
	}

	@Override
	public SearchRes getAllQuizs() {
		List<Quiz> list = quizDao.getAll();
		return new SearchRes(ResCodMessage.SUCCESS.getCode(), ResCodMessage.SUCCESS.getMessage(), list);
	}

	@Override
	public QuestionRes getQuizsByQuizId(int quizId) {
		List<QuestionVO> questionVoList = new ArrayList<>();
		List<Question> list = questionDao.getQuestionByQuizId(quizId);
		// 把每題選項的資料型態從 String 轉換成 List<String>
		for (Question item : list) {
			String str = item.getOption();
			try {
				List<String> optionList = mapper.readValue(str, new TypeReference<>() {
				});
				// 將從DB取得的每一筆資料(Question item) 的每個欄位值放到 QuestionVo 中，以便返回給使用者
				// Question 和 QuestionVO 的差別在於 選項 的資料型態
				QuestionVO vo = new QuestionVO(item.getQuizid(), item.getQuestionid(), item.getQuestion(), //
						item.getType(), item.isRequired(), optionList);
				// 把每個 vo 放到 questionVoList 中
				questionVoList.add(vo);
			} catch (Exception e) {
				// 這邊不寫 throw e 是因為次方法中沒有使用 @Transactional，不影響返回結果
				return new QuestionRes(ResCodMessage.OPTIONS_TRANSFER_ERROR.getCode(), //
						ResCodMessage.OPTIONS_TRANSFER_ERROR.getMessage());
			}
		}
		return new QuestionRes(ResCodMessage.SUCCESS.getCode(), //
				ResCodMessage.SUCCESS.getMessage(), questionVoList);
	}

	@Override
	public SearchRes search(SearchReq req) {
		// 轉換 req 的值
		// quizName 是 null，轉成空字串
		String quizName = req.getQuizName();
		if (quizName == null) {
			quizName = "";
		} else {// 多餘的，不需要寫，但為了理解下面的3元運算子而寫
			quizName = quizName;
		}
		// 3元運算子
		// 格式: 變數名稱 = 條件判斷式 ? 判斷式結果為 true 時要賦予的值 : 判斷式結果為 false時要賦予的ㄓ˙
		quizName = quizName == null ? "" : quizName;
		// 上面的程式碼可以只用下面一行來取得值
		String quizString = req.getQuizName() == null ? "" : req.getQuizName();
		// ==========================================
		// 轉換 開始時間 --> 若沒有給開始日期 --> 給定一個很早的時間
		LocalDate startDate = req.getStartDate() == null ? LocalDate.of(1970, 1, 1) : req.getStartDate();

		LocalDate endDate = req.getEndDate() == null ? LocalDate.of(2999, 12, 31) : req.getEndDate();

		List<Quiz> list = new ArrayList<>();

		if (req.isPublished()) {
			list = quizDao.getAllPublished(quizName, startDate, endDate);
		} else {
			list = quizDao.getAll(quizName, startDate, endDate);
		}

		return new SearchRes(ResCodMessage.SUCCESS.getCode(), ResCodMessage.SUCCESS.getMessage(), list);
	}

	@Transactional(rollbackFor = Exception.class)
	@Override
	public BasicRes delete(int quizId) throws Exception {
		if (quizId <= 0) {
			return new BasicRes(ResCodMessage.QUIZ_ID_ERROR.getCode(), ResCodMessage.QUIZ_ID_ERROR.getMessage());
		}
		try {
			quizDao.deleteById(quizId);
			questionDao.deleteByQuizId(quizId);
		} catch (Exception e) {
			// 不能 return BasicRes 而是要將發生的異常拋出去，這樣 @Transaction 才會生效
			throw e;
		}
		return null;
	}
}