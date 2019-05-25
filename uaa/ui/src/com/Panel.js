
import * as React from "react";
import * as PropTypes from "prop-types";
import { ThemeType } from 'react-uwp/Theme';

/**
 * default value:
 * * width: 300
 * * height: 300
 */
export default class Panel extends React.Component {

    static contextTypes = { theme: PropTypes.object }
    static context = { theme: ThemeType }
    
    render() {
        const { className, style, children } = this.props
        const { theme } = this.context
        return (
            <div className={className} style={{width: 300, height: 300,
                background: theme.acrylicTexture40.background,
                ...style
            }}>{children}</div>
        )
    }
}
