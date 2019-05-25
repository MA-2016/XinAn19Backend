
import * as React from "react";

/**
 * default value:
 * * fill: string
 */
export default class Panel extends React.Component {

    render() {
        const { fill, style, children } = this.props
        return (
            <div style={{
                display: 'inline-block',
                verticalAlign: 'middle',
                wordBreak: 'break-all',
                wordWrap: 'break-word',
                borderRadius: 3,
                margin: 2, padding: '0px 5px',
                backgroundColor: fill || 'rgb(100, 100, 100)',
                ...style
            }}>{children}</div>
        )
    }
}
