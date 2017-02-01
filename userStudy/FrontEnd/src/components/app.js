import React from 'react'
import { connect } from 'react-redux'

import Landing from './auth/landing'
import Question from './question/question'
import End from './end/end'



const App = ({location}) => {
	let view;
	if (location === 'QUESTION_PAGE') {
		view = <Question/>;
	} 
	else if (location === 'END_PAGE') {
		view = <End/>;
	} 
	else{
		view = <Landing/>
	}
	return (
		<div>
			{view}
		</div>
	)
}

export default connect((state) => {
	return {location:state.shared.location}
})(App)