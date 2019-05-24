import * as React from "react";
import PasswordBox from "react-uwp/PasswordBox";
import Image from "react-uwp/Image";
import TextBox from "react-uwp/TextBox";

/**
 * 登录界面
 */
export default class SignIn extends React.Component {

    render(){
        return (
            <div style={{
                width: 400,
                height: 400,
                margin: '0px auto',
                textAlign: 'center',
                position: 'absolute',
                top: '50%', left: '50%',
                marginLeft: -200,
                marginTop: -200,
            }}>
                <Image useLazyLoad
                    style={{ width: 120, height: 120, borderRadius: '50%', margin: '10px 0px' }}
                    src="" />
                <TextBox
                    style={inputBoxStyle}
                    placeholder="输入帐号" />
                <PasswordBox
                    style={inputBoxStyle}
                    placeholder="输入密码"
                    passwordBoxHeight={28} />
            </div>
        )
    }

}

const inputBoxStyle = {
    width: 300, margin: '10px auto'
}