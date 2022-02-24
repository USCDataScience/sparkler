import * as types from './types';


// Initial (starting) state
const initialState = {
    currentTime: new Date().toString()
}

// Our root reducer starts with the initial state
// and must return a representation of the next state
const rootReducer = (state = initialState, action) => {
    switch(action.type) {
        case types.FETCH_NEW_TIME:
            return { ...state, currentTime: action.payload}
        default:
            return state;
    }
}

export default rootReducer