import Action, {resource, displayErrorMsg} from '../../actions'
import {surveyConfig} from './descriptionConst'


export function getQuestion(){
	return (dispatch) => {
		return resource('GET', 'questions')
		.then((response)=>{
			const questions = response.questions;
			dispatch({type:Action.UPDATEQUESTION, questions});
		})
	}
}

export function checkOneQuestion(index, answer, time){
	return (dispatch, getState) => {
		let questions = getState().questions.questions;
		questions[index].answer = answer;
		questions[index].time = time;
		return dispatch({type:Action.ADDANSWER, questions,index});
	}
}

export function checkSurvey(answers){
	return (dispatch) => {
		if(Object.keys(answers).length === surveyConfig.length){
			return dispatch({type:Action.UPDATESURVEY, answers});
		}
		else{
			return dispatch(displayErrorMsg("Please finish all survey questions!"));
		}
	}
}